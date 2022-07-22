package com.example.travellens;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Filter {
    private static Filter instance = new Filter();

    public final static List<String> TYPE_LIST = new ArrayList<>(Arrays.asList("AIRPORT",
            "AMUSEMENT_PARK","AQUARIUM", "ART_GALLERY", "BAKERY","BOOK_STORE","CAFE","CAMPGROUND",
            "CAR_RENTAL" , "CITY_HALL", "CLOTHING_STORE", "CONVENIENCE_STORE", "FLORIST", "FOOD", "LIBRARY",
            "LODGING", "MEAL_DELIVERY", "MEAL_TAKEAWAY","MUSEUM",  "PARK", "POINT_OF_INTEREST", "RESTAURANT", "SHOPPING_MALL",
            "SPA", "STORE", "SUBWAY_STATION", "TOURIST_ATTRACTION", "TRAIN_STATION", "TRANSIT_STATION", "TRAVEL_AGENCY", "ZOO"));
    public final static List<String> TYPE_LIST_VIEW = new ArrayList<>(Arrays.asList("airport",
            "amusement park","aquarium", "art gallery", "bakery","book store","cafe","campground",
            "car rental" , "city hall", "clothing store", "convenience store", "florist", "food", "library",
            "lodging", "meal delivery", "meal takeaway","museum",  "park", "point of interest", "restaurant", "shopping mall",
            "spa", "store", "subway station", "tourist attraction", "train station", "transit station", "travel agency", "zoo"));

    private Filter() {}

    public static Filter getInstance() {
        return instance;
    }

    public List<Post> getPostsByFiltering(List<String> types, List<Post> currentPosts) {
        List<String> customWords = new ArrayList<>();
        List<String> typesWords = new ArrayList<>();
        for (String wordOrType: types) {
            if (TYPE_LIST.contains(wordOrType)) {
                typesWords.add(wordOrType);
            } else {
                customWords.add(wordOrType);
            }
        }

        // gets posts that match all the elements in the custom words
        List<Post> postsByCustom = getPostsByWords(customWords, currentPosts);
        // gets all the elements that match all in the types words
        List<Post> postsByType = getPostsByType(typesWords, postsByCustom);

        // now go through each string in types since they're in order, if the post contains the first type,
        // add to list. If its already in similar posts, do not add it again. This way we maintain order of types and posts
        List<Post> inOrderFilteredPosts = getLeftoverPostsThatMatch(postsByType, types, currentPosts);
        return inOrderFilteredPosts;
    }

    private List<Post> getLeftoverPostsThatMatch(List<Post> similarPosts, List<String> types, List<Post> allPosts) {
        List<Post> filteredPosts = new ArrayList<>(similarPosts);
        for (Post post : allPosts) {
            for (String typeOrWord : types) {
                if (TYPE_LIST.contains(typeOrWord) && post.getList(Post.KEY_TYPES).contains(typeOrWord) && !filteredPosts.contains(post)) {
                    // treat it as a type
                    filteredPosts.add(post);
                }
                if ((post.getDescription().contains(typeOrWord) || post.getString(Post.KEY_PLACE_NAME).contains(typeOrWord)) && !filteredPosts.contains(post)) {
                    // treat it as a custom word
                    filteredPosts.add(post);
                }
            }
        }
        return filteredPosts;
    }

    private List<Post> getPostsByType(List<String> types, List<Post> currentPosts) {
        if (types.size() == 0) {
            return currentPosts;
        }
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post: currentPosts) {
            if (containsAllTypes(post.getList(Post.KEY_TYPES), types)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }

    private List<Post> getPostsByWords(List<String> words, List<Post> currentPosts) {
        if (words.size() == 0) {
            return currentPosts;
        }
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post: currentPosts) {
            if (containsAllWords(post.getDescription(), words)
                    || (containsAllWords(post.getString(Post.KEY_PLACE_NAME), words))) {
                filteredPosts.add(post);
            }
        }
        return filteredPosts;
    }

    private boolean containsAllTypes(List<String> typesInPost, List<String> typesToFilter) {
        for (String filter : typesToFilter)
            if (!typesInPost.contains(filter)) return false;
        return true;
    }

    private boolean containsAllWords(String description, List<String> typesToFilter) {
        for (String filter : typesToFilter)
            if (!description.contains(filter)) return false;
        return true;
    }
}
