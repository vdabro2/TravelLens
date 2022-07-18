package com.example.travellens;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseUser;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public static final int INCOMING_MESSAGE_TYPE = 0; // incoming
    public static final int OUTGOING_MESSAGE_TYPE = 1; // outcoming
    private Context context;
    private List<Message> messages;
    private Post postClickedOn;

    public MessageAdapter(Context context, List<Message> messages, Post post) {
        this.context = context;
        this.messages = messages;
        this.postClickedOn = post;
    }

    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return OUTGOING_MESSAGE_TYPE;
        } else {
            return INCOMING_MESSAGE_TYPE;
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

        if (viewType == INCOMING_MESSAGE_TYPE) {
            View contactView = inflater.inflate(R.layout.item_incoming_message, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else {
            View contactView = inflater.inflate(R.layout.item_outgoing_message, parent, false);
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

    private class IncomingMessageViewHolder extends MessageViewHolder {
        ImageView imageOther;
        TextView body;

        public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            imageOther = (ImageView)itemView.findViewById(R.id.ivRecipientProfilePic);
            body = (TextView)itemView.findViewById(R.id.tvMessage);
        }

        @Override
        public void bindMessage(Message message) {
            body.setText(message.getMessage());
            Glide.with(context)
                    .load(postClickedOn.getUser().getParseFile(Post.KEY_PROFILE_PICTURE).getUrl())
                    .circleCrop()
                    .into(imageOther);

        }
    }

    private class OutgoingMessageViewHolder extends MessageViewHolder {
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
            Glide.with(context)
                    .load(ParseUser.getCurrentUser().getParseFile(Post.KEY_PROFILE_PICTURE).getUrl())
                    .circleCrop()
                    .into(imageMe);

        }
    }



}


