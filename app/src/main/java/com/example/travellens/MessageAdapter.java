package com.example.travellens;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseUser;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public static final int MESSAGE_LEFT = 0; // incoming
    public static final int MESSAGE_RIGHT = 1; // outcoming
    private Context context;
    private List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return MESSAGE_RIGHT;
        } else {
            return MESSAGE_LEFT;
        }
    }

    private boolean isMe(int position) {
        Message message = messages.get(position);
        return message.getSender() != null && message.getSender()
                .equals(ParseUser.getCurrentUser().getString(Post.KEY_FIREBASE_USER_ID));
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == MESSAGE_LEFT) {
            View contactView = inflater.inflate(R.layout.item_left_chat, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else {
            View contactView = inflater.inflate(R.layout.item_right_chat, parent, false);
            return new OutgoingMessageViewHolder(contactView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bindMessage(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(Message message);
    }

    public class IncomingMessageViewHolder extends MessageViewHolder {
        ImageView imageOther;
        TextView body;
        TextView name;

        public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            imageOther = (ImageView)itemView.findViewById(R.id.ivRecipientProfilePic);
            body = (TextView)itemView.findViewById(R.id.tvMessage);
            name = (TextView)itemView.findViewById(R.id.tvRecipientName);
        }

        @Override
        public void bindMessage(Message message) {
            body.setText(message.getMessage());
            name.setText(message.getSender());

        }
    }

    public class OutgoingMessageViewHolder extends MessageViewHolder {
        ImageView imageMe;
        TextView body;

        public OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            imageMe = (ImageView)itemView.findViewById(R.id.ivRecipientProfilePic2);
            body = (TextView)itemView.findViewById(R.id.tvMessage2);
        }

        @Override
        public void bindMessage(Message message) {
            body.setText(message.getMessage());
        }
    }



}


