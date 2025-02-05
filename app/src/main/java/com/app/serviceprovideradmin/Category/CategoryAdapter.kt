package com.app.serviceprovideradmin.Category

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class CategoryAdapter(private val context: Context, private val categoryList: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryImage: ImageView = itemView.findViewById(R.id.image)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val modifydata:ImageView=itemView.findViewById(R.id.deletedata)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.categoryName.text = category.name
        Glide.with(holder.itemView.context)
            .load(category.imageUrl)
            .circleCrop()
            .into(holder.categoryImage)

        holder.modifydata.setOnClickListener {
            modifydata(category)
        }
    }

    private fun modifydata(category: Category){

        val dialogview=LayoutInflater.from(context).inflate(R.layout.updatecategory,null)

        val categoryImage: ImageView = dialogview.findViewById(R.id.image)
        val categoryName: EditText = dialogview.findViewById(R.id.categoryname)
        val updateButton: Button = dialogview.findViewById(R.id.update)
        val deleteButton: TextView = dialogview.findViewById(R.id.delete)
        val remove:ImageView=dialogview.findViewById(R.id.remove)

        // Set category details
//        categoryName.text = category.name
        Glide.with(context)
            .load(category.imageUrl) // Load the category's image
            .circleCrop() // Optional: Display the image in a circular shape
            .into(categoryImage)

        // Build the AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(dialogview)
        val dialog = dialogBuilder.create()

        updateButton.setOnClickListener {
            val updatedName = categoryName.text.toString().trim()
            if (updatedName.isNotEmpty()) {
                // Create a HashMap to hold the updated data
                val updates = HashMap<String, Any>()
                updates["name"] = updatedName

                // Reference the specific category in the database
                val categoryRef = FirebaseDatabase.getInstance()
                    .getReference("categories")
                    .child(category.key!!) // Assuming 'key' is the unique identifier

                // Update the data in Firebase
                categoryRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Category updated successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update category: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            // Handle the delete action
            dialog?.dismiss()
            Toast.makeText(context,"delete",Toast.LENGTH_SHORT).show()
        }

        remove.setOnClickListener {

            if (category.key != null) {
                val categoryRef = FirebaseDatabase.getInstance()
                    .getReference("categories")
                    .child(category.key)

                categoryRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to delete: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }

        }

        dialog.show()
    }

    }