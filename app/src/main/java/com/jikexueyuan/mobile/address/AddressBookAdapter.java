package com.jikexueyuan.mobile.address;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jikexueyuan.mobile.address.bean.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by aven on 10/23/15.
 */
public class AddressBookAdapter extends RecyclerView.Adapter<AddressBookAdapter.ViewHolder> {
    public List<User> userList = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater in = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = in.inflate(R.layout.address_cell_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = getUser(position);
        holder.username.setText(user.getUsername());
        holder.shorthand.setText("A");
        holder.itemView.setTag(R.id.user_name, user);

    }

    private User getUser(int position) {
        return userList.get(position);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void addUsers(List<User> list) {
        userList.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView shorthand;
        TextView username;
        ImageView avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            shorthand = (TextView) itemView.findViewById(R.id.shorthand);
            username = (TextView) itemView.findViewById(R.id.user_name);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object data = v.getTag(R.id.user_name);
                    if (data instanceof User) {
                        Intent intent = new Intent(v.getContext(), UserDetailActivity.class);
                        intent.putExtra("key_user", ((User) data).json());
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }

}
