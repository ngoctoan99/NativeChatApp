package com.example.nativeandroidapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.activity.ChatActivity;
import com.example.nativeandroidapp.activity.ThereProfileActivity;
import com.example.nativeandroidapp.models.ModelUsers;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class AdapterHashTag extends ArrayAdapter<ModelUsers> {
    private final List<ModelUsers> searchList;
    public AdapterHashTag(@NonNull Context context, int resource, @NonNull List<ModelUsers> objects) {
        super(context, resource, objects);
        searchList = new ArrayList<>(objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_hashtag,parent,false);
        }
        ImageView imagesearch = convertView.findViewById(R.id.imageHashTag);
        TextView tvname = convertView.findViewById(R.id.nameHashTagTv);
        TextView tvemail = convertView.findViewById(R.id.emailHashTagTv);
        ModelUsers user = getItem(position);
        try{
            Picasso.get().load(user.getImage())
                    .placeholder(R.drawable.ic_face_custom)
                    .into(imagesearch);
        }catch (Exception e){
            imagesearch.setImageResource(R.drawable.ic_face_custom);
        }
        tvname.setText(user.getName());
        tvemail.setText(user.getEmail());
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ModelUsers> list = new ArrayList<>();
                if(constraint == null || constraint.length() == 0 ){
                    list.addAll(searchList);
                }else if(constraint.toString().charAt(0) == '@'){
                    String filter = constraint.toString().toLowerCase().trim().substring(1);
                    for(ModelUsers sp : searchList){
                        if(sp.getName().toLowerCase().contains(filter)){
                            list.add(sp);
                        }
                    }
                }
//                else {
//                    String filter = constraint.toString().toLowerCase().trim();
//                    for(ModelUsers sp : searchList){
//                        if(sp.getName().toLowerCase().contains(filter)){
//                            list.add(sp);
//                        }
//                    }
//                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = list;
                filterResults.count = list.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                addAll((List<ModelUsers>) results.values);
                notifyDataSetInvalidated();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((ModelUsers)resultValue).getName();
            }
        };
    }
}
