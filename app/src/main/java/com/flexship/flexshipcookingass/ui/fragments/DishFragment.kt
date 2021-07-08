package com.flexship.flexshipcookingass.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.other.collapse
import com.flexship.flexshipcookingass.other.zeroOrNotZero
import com.flexship.flexshipcookingass.ui.dialogs.MinutePickerDialog
import com.flexship.flexshipcookingass.ui.viewmodels.DishViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog

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
    private var stageList: MutableList<Stages> = mutableListOf()
    private var bufStageList: MutableList<Stages> = mutableListOf()

    private var isNewDish: Boolean = true
    private var isCreatedNewDish:Boolean=false

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
                            "Handling permissions",
                            "Please accept all the permissions to be able to set an image of your dish...",
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeSec = savedInstanceState?.getInt(Constans.KEY_MINUTE) ?: 0


        if (savedInstanceState != null) {
            setTimeToButton(timeSec)
            val numberPickerDialog = parentFragmentManager
                .findFragmentByTag(Constans.TAG_MINUTE_PICKER) as MinutePickerDialog?

            numberPickerDialog?.setAction { time ->
                this.timeSec = time
                setTimeToButton(timeSec)
            }
        }

        checkPermissions()

        setupUI()


        if (args.dishId != -1) {
            isNewDish = false
            dishId = args.dishId
            viewModel.getDishById(args.dishId).observe(viewLifecycleOwner) { dishWithStages ->
                dish = dishWithStages.dish
                stageList = dishWithStages.stages.toMutableList()
                bufStageList = dishWithStages.stages.toMutableList()
                setValues()
                setTitle("Блюдо:".plus(dish.name))
            }
            binding.bInsertDish.text = "Обновить"
        } else {
            dish = Dish()
            viewModel.insertDish(dish)
            setTitle("Новое блюдо")
            viewModel.getNewDish().observe(viewLifecycleOwner) {
                dishId=1
                it?.let {
                    dishId = it
                }
            }
        }


    }

    private fun setTitle(titleT: String) {
        requireActivity().findViewById<Toolbar>(R.id.main_toolbar).apply {
            title=titleT
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }


    private fun setTimeToButton(timeSec: Int) {
        binding.bTime.text = String.format(
            getString(R.string.timer),
            zeroOrNotZero(timeSec / 60),
            zeroOrNotZero(timeSec % 60)
        )
    }


    private fun setValues() = with(binding) {
        bitmap = dish.image
        edName.setText(dish.name)
        edReceipt.setText(dish.recipe)
        if (dish.image != null)
            mainImage.setImageBitmap(dish.image)
        else
            mainImage.setImageResource(R.drawable.empty)
        stageAdapter.differ.submitList(stageList.toList())
        spinnerCategory.setSelection(dish.category)
    }

    override fun onStart() = with(binding) {
        super.onStart()

        bAddImage.setOnClickListener {
            showPopupMenu()
        }

        bAddStage.setOnClickListener {
            checkFieldsForAddStage()
        }

        bInsertDish.setOnClickListener {
            checkFieldsToAddNewDishOrUpdate()
        }

        bTime.setOnClickListener {
            showMinutePickerDialog()
        }

    }

    private fun setupUI() {

        binding.recViewStages.apply {
            layoutManager = LinearLayoutManager(context)
            stageAdapter = StageAdapter(context)
            adapter = stageAdapter


        }.also {
            val itemListener = ItemTouchHelper(object : DragAndDropSwappable(requireContext()) {
                override fun swapList(startPosition: Int, targetPosition: Int) {

                }

                override fun saveInDatabase() {

                }

                override fun itemEdit(pos: Int) {
                    //лень
                }

                override fun itemDelete(pos: Int) {
                    val stage = stageAdapter.differ.currentList[pos]
                    if (stage.isSaved == 1) viewModel.deleteStage(stage)
                    else {
                        stageList.removeAt(pos)
                        stageAdapter.differ.submitList(stageList.toList())
                    }
                }

            })
            itemListener.attachToRecyclerView(it)
        }


        val textForSpinner =
            arrayOf("Супы", "Закуски", "Салаты", "Пиццы", "Горячее", "Завтрак", "Десерты")
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

    private fun checkFieldsForAddStage() {
        binding.apply {
            val stageName = edStages.text.toString()
            if (stageName.isNotEmpty()) {
                submitNewStage(stageName)
            } else {
                Snackbar.make(requireView(), "Введите название этапа", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun submitNewStage(stageName:String){
        val stage = Stages(name = stageName, time = timeSec.toLong(), dishId = dishId)
        viewModel.insertStage(stage)
        stageList.add(stage)
        stageAdapter.differ.submitList(stageList.toList())
        timeSec = 0
        binding.edStages.setText("")
        binding.bTime.setText(R.string.dish_choose_time)
    }

    private fun checkFieldsToAddNewDishOrUpdate() = with(binding) {
        val name = edName.text.toString()
        val desc = edReceipt.text.toString()
        if (name.isEmpty() && desc.isEmpty())
            Snackbar.make(requireView(), "Введите имя и рецепт блюда", Snackbar.LENGTH_SHORT).show()
        else if (bitmap == null)
            alertDialog(
                "Изображение блюда",
                "Вы не выбрали изображение для блюда.Вы действительно хотите продолжить без изображения блюда?",
                true
            )
        else if (stageList.size == 0)
            alertDialog(
                "Этапы готовки",
                "У вас отсутствуют этапы готовки.Вы действительно хотите продолжить без этапов готовки?",
                true
            )
        else if (name.isEmpty())
            Snackbar.make(requireView(), "Введите имя  блюда", Snackbar.LENGTH_SHORT).show()
        else if (desc.isEmpty())
            Snackbar.make(requireView(), "Введите рецепт блюда", Snackbar.LENGTH_SHORT).show()
        else {
            updateDish(name, desc)
        }
    }

    private fun updateDish(name: String, desc: String) = with(binding) {
        val spinnerPos = spinnerCategory.selectedItemPosition
        val dish = Dish(dishId, name, desc, spinnerPos, bitmap)
        this@DishFragment.dish=dish

        viewModel.updateDish(dish)
        if(isNewDish)
            isCreatedNewDish=true
        findNavController().popBackStack()

    }

    private fun insertNewStagesBeforeUpdate() {
        var tr = false
        if (stageList != bufStageList) {
            for (stage in stageList) {
                for (buf in bufStageList) {
                    if (stage.id == buf.id)
                        tr = true
                }
                if (!tr) {
                    stage.isSaved = 1
                    viewModel.insertStage(stage)
                }
                tr = false
            }
        }
    }

    private fun showMinutePickerDialog() = MinutePickerDialog().apply {
        setAction { seconds ->
            timeSec = seconds
            setTimeToButton(timeSec)
        }
    }.show(parentFragmentManager, Constans.TAG_MINUTE_PICKER)

    override fun onStop() {
        super.onStop()

        if(!isCreatedNewDish && isNewDish){
            dish.id=dishId
            if(stageList.size>0){
                viewModel.deleteDish(dish,true)
            }else{
                viewModel.deleteDish(dish)
            }
        }
    }


    //ЭТО не ТРОГАТЬ
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
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From camera")
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
                    "Handling permissions",
                    "Please accept all the permissions to be able to set an image of your dish...",
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
            .setPositiveButton("Да") { _, _ ->
                if (!isImage) {
                    requestPermissions()
                } else {
                    val name = binding.edName.text.toString()
                    val desc = binding.edReceipt.text.toString()
                    updateDish(name, desc)
                }
            }
            .setNegativeButton("Нет", null)
            .create()
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constans.KEY_MINUTE, timeSec)
    }


}