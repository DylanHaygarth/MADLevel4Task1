package com.example.madlevel4task1

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_shopping.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShoppingFragment : Fragment() {
    private lateinit var productRepository: ProductRepository
    private val products = arrayListOf<ShoppingProduct>()
    private val productAdapter = ProductAdapter(products)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shopping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets up the repository with the required context from this class
        productRepository = ProductRepository(requireContext())
        getShoppingListFromDatabase()

        // sets up the recycler view
        initViews()

        fabAdd.setOnClickListener {
            showAddProductdialog();
        }
    }

    private fun initViews() {
        // Initialize the recycler view with a linear layout manager, adapter
        rvProducts.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvProducts.adapter = productAdapter
        rvProducts.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        // Attaches item touch helper to recycler view
        createItemTouchHelper().attachToRecyclerView(rvProducts)
    }

    // retrieves the products from the database. Clears all products currently in there and replaces with retrieved ones.
    private fun getShoppingListFromDatabase() {
        mainScope.launch {
            val shoppingList = withContext(Dispatchers.IO) {
                productRepository.getAllProducts()
            }
            this@ShoppingFragment.products.clear()
            this@ShoppingFragment.products.addAll(shoppingList)
            this@ShoppingFragment.productAdapter.notifyDataSetChanged()
        }
    }

    // shows the dialog from which a product/amount can be added
    private fun showAddProductdialog() {
        // creates builder and sets the title of the dialog window
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_product_dialog_title))

        // the layout file gets linked to the dialog
        val dialogLayout = layoutInflater.inflate(R.layout.add_product_dialog, null)

        // the filled in product/quantity
        val productName = dialogLayout.findViewById<EditText>(R.id.etProductName)
        val amount = dialogLayout.findViewById<EditText>(R.id.etAmount)

        // the confirm button gets created
        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.dialog_ok_btn) { _: DialogInterface, _: Int ->
            addProduct(productName, amount)
        }
        builder.show()
    }

    // adds the product to the database
    private fun addProduct(txtProductName: EditText, txtAmount: EditText) {
        // checks if input fields are not blank
        if (validateFields(txtProductName, txtAmount)) {
            mainScope.launch {
                // creates a 'ShoppingProduct' variable from the given input fields
                val product = ShoppingProduct(
                    name = txtProductName.text.toString(),
                    quantity = txtAmount.text.toString().toInt()
                )

                // inserts product into database
                withContext(Dispatchers.IO) {
                    productRepository.insertProduct(product)
                }

                // refreshes the shopping list
                getShoppingListFromDatabase()
            }
        }
    }

    // checks if input fields are not blank
    private fun validateFields(txtProductName: EditText, txtAmount: EditText): Boolean {
        return if (txtProductName.text.toString().isNotBlank() && txtAmount.text.toString().isNotBlank()
        ) {
            true
        } else {
            Toast.makeText(activity, "Please fill in the fields", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // gets the position of the product based on where user swipes
                val position = viewHolder.adapterPosition
                val productToDelete = products[position]

                // deletes product from database on swiped location
                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        productRepository.deleteProduct(productToDelete)
                    }
                    getShoppingListFromDatabase()
                }
            }
        }
        return ItemTouchHelper(callback)
    }
}