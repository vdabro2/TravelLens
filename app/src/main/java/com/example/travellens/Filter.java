package com.example.travellens;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    public static List<Post> getPostsByType(List<String> types, List<Post> currentPosts) {
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post: currentPosts) {
            boolean containsType = post.getList(Post.KEY_TYPES).stream().anyMatch(element -> types.contains(element));
            if (containsType)
                filteredPosts.add(post);
        }
        return filteredPosts;
    }

    public static List<Post> getPostsByWords(List<String> words, List<Post> currentPosts) {
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post: currentPosts) {
            // if the description contains any 1 word of the list, add it
            // if the place name contains any 1 word of the list, add it
        }
        return filteredPosts;
    }
}
