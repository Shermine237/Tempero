package com.shermine237.tempora.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shermine237.tempora.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private final CategoryListener listener;
    private final Context context;

    public interface CategoryListener {
        void onCategoryEdit(int position, String newName);
        void onCategoryDelete(int position);
    }

    public CategoryAdapter(List<String> categories, CategoryListener listener, Context context) {
        this.categories = categories;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<String> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textCategoryName;
        private final ImageButton buttonEditCategory;
        private final ImageButton buttonDeleteCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.text_category_name);
            buttonEditCategory = itemView.findViewById(R.id.button_edit_category);
            buttonDeleteCategory = itemView.findViewById(R.id.button_delete_category);
        }

        public void bind(String category, int position) {
            textCategoryName.setText(category);

            buttonEditCategory.setOnClickListener(v -> showEditDialog(category, position));
            buttonDeleteCategory.setOnClickListener(v -> showDeleteDialog(position));
        }

        private void showEditDialog(String currentName, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Modifier la catégorie");

            final EditText input = new EditText(context);
            input.setText(currentName);
            builder.setView(input);

            builder.setPositiveButton("Enregistrer", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    listener.onCategoryEdit(position, newName);
                }
            });
            builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

            builder.show();
        }

        private void showDeleteDialog(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Supprimer la catégorie");
            builder.setMessage("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

            builder.setPositiveButton("Oui", (dialog, which) -> listener.onCategoryDelete(position));
            builder.setNegativeButton("Non", (dialog, which) -> dialog.cancel());

            builder.show();
        }
    }
}
