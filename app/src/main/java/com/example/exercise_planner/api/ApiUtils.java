package com.example.exercise_planner.api;

import retrofit2.Response;

public class ApiUtils {
    public static ApiService getApiService() {
        return ApiClient.getClient().create(ApiService.class);
    }

    public static <T> int getResponseStatusCode(Response<T> response) {
        if (response == null) {
            return 404;
        }
        return response.code();
    }
}
