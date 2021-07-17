package com.flexship.flexshipcookingass.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.adapters.SpinnerAdapter
import com.flexship.flexshipcookingass.adapters.StageAdapter
import com.flexship.flexshipcookingass.databinding.FragmentDishBinding
import com.flexship.flexshipcookingass.helpers.DragAndDropSwappable
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import com.flexship.flexshipcookingass.other.Constants
import com.flexship.flexshipcookingass.other.zeroOrNotZero
import com.flexship.flexshipcookingass.ui.dialogs.DialogFragmentToDelete
import com.flexship.flexshipcookingass.ui.dialogs.MinutePickerDialog
import com.flexship.flexshipcookingass.ui.other.MainActivity
import com.flexship.flexshipcookingass.ui.viewmodels.DishViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import java.io.ByteArrayOutputStream


@AndroidEntryPoint
class DishFragment : Fragment() {

    private lateinit var stageAdapter: StageAdapter

    private lateinit var _binding: FragmentDishBinding
    private val binding get() = _binding

    private val args: DishFragmentArgs by navArgs()
    private val viewModel: DishViewModel by viewModels()

    private lateinit var dish: Dish

    private var bitmap: Bitmap? = null
    private var imageUri: Uri? = null

    private var timeSec: Int = 0
    private var dishId: Int = -1

    private var stageId: Int = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeSec = savedInstanceState?.getInt(Constants.KEY_MINUTE) ?: 0
        bitmap = savedInstanceState?.getByteArray(Constants.KEY_BITMAP)?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
        timeSec = savedInstanceState?.getInt(Constants.KEY_TIME_SEC) ?: 0

        setNotSavedValues()


        if (savedInstanceState != null) {
            dish = savedInstanceState.getSerializable(Constants.KEY_DISH) as Dish

            setTimeToButton(timeSec)
            val numberPickerDialog = parentFragmentManager
                .findFragmentByTag(Constants.TAG_MINUTE_PICKER) as MinutePickerDialog?

            numberPickerDialog?.setAction { time ->
                this.timeSec = time
                setTimeToButton(timeSec)
            }

            val dialogToDelete =
                parentFragmentManager.findFragmentByTag(Constants.TAG_DIALOG_DELETE) as DialogFragmentToDelete?

            dialogToDelete?.setAction {
                findNavController().popBackStack()
            }
        }

        checkPermissions()

        setupUI()

        if (!viewModel.isChangedConfig)
            viewModel.postEmptyValues()

        if (args.dishId != -1) {
            viewModel.isNewDish = false
            dishId = args.dishId
            viewModel.getDishById(dishId).observe(viewLifecycleOwner) { dishWithStages ->

                if (!viewModel.isUpdated) {
                    if (!viewModel.isChangedConfig) {
                        dish = dishWithStages.dish
                        setValues()
                    }
                    viewModel.isUpdated = true
                    setupActionBar(String.format(getString(R.string.meal), dish.name))
                }
                viewModel.mutableStageList.postValue(dishWithStages.stages.toMutableList())
            }
            binding.bInsertDish.text = getString(R.string.update)
        } else {
            dish = Dish()
            viewModel.getMaxIdDish().observe(viewLifecycleOwner) {
                dishId = 1
                it?.let {
                    dishId = it + 1
                }
            }
            setupActionBar(getString(R.string.new_meal))
        }
        viewModel.getMaxIdStage.observe(viewLifecycleOwner) {
            stageId = 1
            it?.let {
                stageId = it + 1
            }
        }
        viewModel.stageList.observe(viewLifecycleOwner) {
            stageAdapter.submitList(it.toList())
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            checkFieldsForUpdateIfNotSaved()
        }


    }

    override fun onStart() = with(binding) {
        super.onStart()

        bAddImage.setOnClickListener {
            showPopupMenu()
        }

        bAddStage.setOnClickListener {
            val stageName = edStages.text.toString()

            if (stageName.isNotEmpty()) {
                if (!viewModel.isStageEdit) {
                    submitNewStage(stageName)
                } else {
                    updateStage(stageName)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    getString(R.string.stage_name_snackbar),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }

        bInsertDish.setOnClickListener {
            checkFieldsToAddNewDishOrUpdate()
        }

        bTime.setOnClickListener {
            showMinutePickerDialog()
        }

    }

    override fun onStop() {
        super.onStop()

        if (!requireActivity().isChangingConfigurations) {
            for (i in viewModel.bufferStageList) {
                viewModel.deleteStage(i)
            }

        } else {
            viewModel.isChangedConfig = true
            viewModel.isUpdated = false
        }

    }

    private fun updateStage(stageName: String) {
        val stage = Stages(
            name = stageName,
            time = timeSec.toLong(),
            dishId = dishId,
            id = viewModel.stageToEdit?.id!!
        )
        viewModel.updateStage(stage)
        if (viewModel.isNewDish) {
            viewModel.mutableStageList.value?.apply {
                set(viewModel.posToEdit, stage)
                viewModel.mutableStageList.postValue(this)
            }
        }
        viewModel.bufferStageList.apply {
            if (contains(viewModel.stageToEdit)) {
                remove(viewModel.stageToEdit)
                add(stage)
            }
        }


        viewModel.isStageEdit = false
        binding.edStages.setText("")
        timeSec = 0
        binding.bTime.text = getString(R.string.specify_time)
        binding.bAddStage.text = getString(R.string.add)
    }

    private fun setNotSavedValues() {
        bitmap?.let {
            binding.mainImage.setImageBitmap(it)
        }
        if (timeSec != 0) {
            setTimeToButton(timeSec)
        }
        if (viewModel.isStageEdit) {
            binding.bAddStage.text = getString(R.string.update_stage)
        }
    }


    private fun setValues() = with(binding) {

        bitmap = dish.image
        edName.setText(dish.name)
        edReceipt.setText(dish.recipe)
        if (dish.image != null)
            mainImage.setImageBitmap(dish.image)
        else
            mainImage.setImageResource(R.drawable.empty)
        spinnerCategory.setSelection(dish.category)

    }


    private fun submitNewStage(stageName: String) {
        val stage = Stages(name = stageName, time = timeSec.toLong(), dishId = dishId)

        viewModel.insertStage(stage)
        stage.id = stageId
        viewModel.bufferStageList.add(stage)
        if (viewModel.isNewDish) {
            viewModel.mutableStageList.value?.apply {
                add(stage)
                viewModel.mutableStageList.postValue(this)
            }
        }
        timeSec = 0
        binding.edStages.setText("")
        binding.bTime.setText(R.string.dish_choose_time)
    }


    private fun updateDish(name: String, desc: String) = with(binding) {
        val spinnerPos = spinnerCategory.selectedItemPosition
        val dish = Dish(dishId, name, desc, spinnerPos, bitmap)
        this@DishFragment.dish = dish
        if (viewModel.isNewDish) {
            viewModel.insertDish(dish)
        } else {
            viewModel.updateDish(dish)
        }
        viewModel.bufferStageList.clear()
        findNavController().popBackStack()

    }


    private fun showMinutePickerDialog() = MinutePickerDialog().apply {
        setAction { seconds ->
            timeSec = seconds
            setTimeToButton(timeSec)
        }
    }.show(parentFragmentManager, Constants.TAG_MINUTE_PICKER)


    private fun checkFieldsForUpdateIfNotSaved() = with(binding) {
        val name = edName.text.toString()
        val receipt = edReceipt.text.toString()
        if (name != dish.name || receipt != dish.recipe || viewModel.bufferStageList.isNotEmpty()) {
            showDialogToDelete()
        } else {
            findNavController().popBackStack()
        }
    }


    private fun showDialogToDelete() = DialogFragmentToDelete.newInstance(
        getString(R.string.dialog_del_mes),
        getString(R.string.dialog_del_title)
    ).apply {
        setAction {
            findNavController().popBackStack()
        }
    }.show(parentFragmentManager, Constants.TAG_DIALOG_DELETE)


    //ЭТО не ТРОГАТЬ


    private fun setTimeToButton(timeSec: Int) {
        binding.bTime.text = String.format(
            getString(R.string.timer),
            zeroOrNotZero(timeSec / 60),
            zeroOrNotZero(timeSec % 60)
        )
    }

    private fun setupActionBar(titleT: String) {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).supportActionBar?.apply {
            title = titleT
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        checkFieldsForUpdateIfNotSaved()
        return true
    }

    private fun setupUI() {

        binding.recViewStages.apply {
            layoutManager = LinearLayoutManager(context)
            stageAdapter = StageAdapter(context)
            adapter = stageAdapter

        }.also {
            val itemListener = ItemTouchHelper(object : DragAndDropSwappable(requireContext()) {

                override fun itemEdit(pos: Int) {
                    viewModel.posToEdit = pos
                    viewModel.isStageEdit = true
                    val stage = stageAdapter.items[pos]
                    viewModel.stageToEdit = stage
                    binding.edStages.setText(stage.name)
                    timeSec = stage.time.toInt()
                    setTimeToButton(timeSec)
                    binding.bAddStage.text = getString(R.string.update_stage)
                }

                override fun itemDelete(pos: Int) {
                    viewModel.isBuffer = false
                    val stage = viewModel.stageList.value?.get(pos)!!
                    viewModel.deleteStage(stage)
                    var stageToDelete: Stages? = null
                    for (buf in viewModel.bufferStageList) {

                        if (stage == buf) {
                            stageToDelete = stage
                            viewModel.isBuffer = true
                        }
                    }
                    stageToDelete?.let {
                        viewModel.bufferStageList.remove(it)
                    }
                    if (viewModel.isNewDish) {
                        viewModel.mutableStageList.value?.apply {
                            removeAt(pos)
                            viewModel.mutableStageList.postValue(this)
                        }
                    }
                    Snackbar.make(
                        requireView(),
                        R.string.stage_has_been_deleted,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.undo) {
                            viewModel.insertStage(stage)
                            if (viewModel.isBuffer) {
                                viewModel.bufferStageList.add(stage)
                            }
                            if (viewModel.isNewDish) {
                                viewModel.mutableStageList.value?.apply {
                                    add(pos, stage)
                                    viewModel.mutableStageList.postValue(this)
                                }
                            }
                        }
                        .show()
                }
            })
            itemListener.attachToRecyclerView(it)
        }


        val textForSpinner =
            arrayOf(
                getString(R.string.soup),
                getString(R.string.snacks),
                getString(R.string.salads),
                getString(R.string.pizza),
                getString(R.string.main_meal),
                getString(R.string.breakfast),
                getString(R.string.sweets)
            )
        val imagesForSpinner = arrayOf(
            R.drawable.soup,
            R.drawable.snack,
            R.drawable.salad,
            R.drawable.pizza,
            R.drawable.thanksgiving,
            R.drawable.breakfast,
            R.drawable.vegan
        )

        binding.spinnerCategory.apply {
            adapter = SpinnerAdapter(requireContext(), textForSpinner, imagesForSpinner)
        }
    }

    private fun checkFieldsToAddNewDishOrUpdate() = with(binding) {
        val name = edName.text.toString()
        val desc = edReceipt.text.toString()
        if (name.isEmpty() && desc.isEmpty())
            Snackbar.make(requireView(), getString(R.string.enter_dish), Snackbar.LENGTH_SHORT)
                .show()
        else if (bitmap == null)
            alertDialog(
                getString(R.string.image),
                getString(R.string.alert_image),
                true
            )
        else if (viewModel.stageList.value?.size ?: 0 == 0)
            alertDialog(
                getString(R.string.stages_miss),
                getString(R.string.alert_stages_miss),
                true
            )
        else if (name.isEmpty())
            Snackbar.make(requireView(), getString(R.string.enter_dish_name), Snackbar.LENGTH_SHORT)
                .show()
        else {
            updateDish(name, desc)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { permission ->
                if (!permission.value) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            android.Manifest.permission.CAMERA
                        )
                        || ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        || ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        alertDialog(
                            getString(R.string.handling_permissions),
                            getString(R.string.handling_permissions_message),
                            false
                        )
                    } else {
                        AppSettingsDialog.Builder(this).build().show()
                    }
                    return@registerForActivityResult
                }
            }
            binding.bAddImage.isEnabled = true
        }
    private val getPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    binding.mainImage.setImageURI(it.data)
                    bitmap = convertUriToBitmap(it.data!!)
                }
            }
        }
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri?.let {
                    binding.mainImage.setImageURI(it)
                    bitmap = convertUriToBitmap(it)
                }
            }
        }


    private fun showPopupMenu() {
        PopupMenu(requireContext(), binding.bAddImage).apply {
            inflate(R.menu.photo_menu)
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.get_photo) {
                    getPhotoFromGallery()
                    return@setOnMenuItemClickListener false
                }
                if (item.itemId == R.id.take_photo) {
                    takePhoto()
                    return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForceShowIcon(true)
            }
        }.also {
            it.show()
        }
    }

    private fun convertUriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                requireContext().contentResolver,
                uri
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
    }

    private fun takePhoto() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, getString(R.string.new_picture))
            put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.from_camera))
        }
        imageUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }.also {
            takePhotoLauncher.launch(it)
        }

    }

    private fun getPhotoFromGallery() {
        Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            type = "image/*"
        }.also {
            getPhotoLauncher.launch(it)
        }
    }

    //PERMISSIONS
    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED -> {
                binding.bAddImage.isEnabled = true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.CAMERA
            )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                alertDialog(
                    getString(R.string.handling_permissions),
                    getString(R.string.handling_permissions_message),
                    false
                )
            }
            else -> {
                requestPermissions()
            }
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun alertDialog(title: String, message: String, isImage: Boolean) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (!isImage) {
                    requestPermissions()
                } else {
                    val name = binding.edName.text.toString()
                    val desc = binding.edReceipt.text.toString()
                    updateDish(name, desc)
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .create()
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constants.KEY_MINUTE, timeSec)
        bitmap?.let {
            val out = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 30, out)
            val bytes = out.toByteArray()
            outState.putByteArray(Constants.KEY_BITMAP, bytes)
        }
        outState.putInt(Constants.KEY_TIME_SEC, timeSec)
        outState.putSerializable(Constants.KEY_DISH, dish)
    }


}