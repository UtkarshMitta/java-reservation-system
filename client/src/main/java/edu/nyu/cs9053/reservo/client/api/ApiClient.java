package edu.nyu.cs9053.reservo.client.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8081";
    private final OkHttpClient client;
    private final Gson gson;
    private String authToken;

    public ApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    private Request.Builder requestBuilder() {
        Request.Builder builder = new Request.Builder();
        if (authToken != null) {
            builder.addHeader("Authorization", authToken);
        }
        return builder;
    }

    public Map<String, Object> login(String username, String password) throws IOException {
        Map<String, String> body = Map.of("username", username, "password", password);
        Request request = requestBuilder()
                .url(BASE_URL + "/auth/login")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                throw new IOException("Login failed: " + responseBody);
            }
        }
    }

    public Map<String, Object> register(String username, String password, String email) throws IOException {
        Map<String, String> body = Map.of(
                "username", username, 
                "password", password,
                "email", email
        );
        Request request = requestBuilder()
                .url(BASE_URL + "/auth/register")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                Map<String, Object> result = gson.fromJson(responseBody, Map.class);
                if (result == null || !result.containsKey("token")) {
                    throw new IOException("Registration failed: Invalid response from server");
                }
                return result;
            } else {
                // Try to parse error message from response
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Registration failed: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Registration failed: " + responseBody);
            }
        }
    }

    public Map<String, Object> getProfile() throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/auth/profile")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                throw new IOException("Failed to get profile: " + responseBody);
            }
        }
    }

    public void updateEmail(String email) throws IOException {
        Map<String, String> body = Map.of("email", email);
        Request request = requestBuilder()
                .url(BASE_URL + "/auth/update-email")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to update email: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to update email: " + responseBody);
            }
        }
    }

    public void changePassword(String oldPassword, String newPassword) throws IOException {
        Map<String, String> body = Map.of(
                "oldPassword", oldPassword,
                "newPassword", newPassword
        );
        Request request = requestBuilder()
                .url(BASE_URL + "/auth/change-password")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to change password: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to change password: " + responseBody);
            }
        }
    }

    public List<Map<String, Object>> getResources() throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/resources")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                return gson.fromJson(responseBody, listType);
            } else {
                throw new IOException("Failed to get resources: " + responseBody);
            }
        }
    }

    public Map<String, Object> getAvailability(Long resourceId, String from, String to) throws IOException {
        String url = String.format("%s/resources/%d/availability?from=%s&to=%s",
                BASE_URL, resourceId, from, to);
        Request request = requestBuilder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                throw new IOException("Failed to get availability: " + responseBody);
            }
        }
    }

    public Map<String, Object> placeHold(Long timeSlotId, Integer qty, String requestId) throws IOException {
        Map<String, Object> body = Map.of(
                "timeSlotId", timeSlotId,
                "qty", qty,
                "requestId", requestId
        );
        Request request = requestBuilder()
                .url(BASE_URL + "/holds")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                throw new IOException("Failed to place hold: " + responseBody);
            }
        }
    }

    public Map<String, Object> confirmHold(Long holdId) throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/holds/" + holdId + "/confirm")
                .post(RequestBody.create("{}", MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                throw new IOException("Failed to confirm hold: " + responseBody);
            }
        }
    }

    public void cancelReservation(Long reservationId) throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/reservations/" + reservationId + "/cancel")
                .post(RequestBody.create("{}", MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to cancel reservation");
            }
        }
    }

    public void cancelHold(Long holdId) throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/holds/" + holdId)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to cancel hold: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to cancel hold: " + responseBody);
            }
        }
    }

    public List<Map<String, Object>> getMyHolds() throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/my-holds")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                return gson.fromJson(responseBody, listType);
            } else {
                throw new IOException("Failed to get holds: " + responseBody);
            }
        }
    }

    public void joinWaitlist(Long timeSlotId) throws IOException {
        Map<String, Long> body = Map.of("timeSlotId", timeSlotId);
        Request request = requestBuilder()
                .url(BASE_URL + "/waitlist")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to join waitlist");
            }
        }
    }

    public List<Map<String, Object>> getMyReservations() throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/my-reservations")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                return gson.fromJson(responseBody, listType);
            } else {
                throw new IOException("Failed to get reservations: " + responseBody);
            }
        }
    }

    public List<Map<String, Object>> getMyWaitlist() throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/my-waitlist")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                return gson.fromJson(responseBody, listType);
            } else {
                throw new IOException("Failed to get waitlist: " + responseBody);
            }
        }
    }

    public List<Map<String, Object>> getNotifications() throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/notifications")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                return gson.fromJson(responseBody, listType);
            } else {
                throw new IOException("Failed to get notifications: " + responseBody);
            }
        }
    }

    // Admin methods
    public Map<String, Object> createResource(Map<String, Object> resource) throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/admin/resources")
                .post(RequestBody.create(gson.toJson(resource), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to create resource: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to create resource: " + responseBody);
            }
        }
    }

    public void deleteResource(Long resourceId) throws IOException {
        Request request = requestBuilder()
                .url(BASE_URL + "/admin/resources/" + resourceId)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to delete resource: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to delete resource: " + responseBody);
            }
        }
    }

    public Map<String, Object> simulateContention(Long timeSlotId, Integer numThreads, Integer qty) throws IOException {
        Map<String, Object> body = Map.of(
                "timeSlotId", timeSlotId,
                "numThreads", numThreads,
                "qty", qty != null ? qty : 1
        );
        Request request = requestBuilder()
                .url(BASE_URL + "/admin/simulate-contention")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to simulate contention: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to simulate contention: " + responseBody);
            }
        }
    }

    public Map<String, Object> setIsolationMode(String level) throws IOException {
        Map<String, String> body = Map.of("level", level);
        Request request = requestBuilder()
                .url(BASE_URL + "/admin/isolation-mode")
                .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                // Try to parse error message
                try {
                    Map<String, Object> errorResponse = gson.fromJson(responseBody, Map.class);
                    if (errorResponse != null && errorResponse.containsKey("error")) {
                        throw new IOException("Failed to set isolation mode: " + errorResponse.get("error"));
                    }
                } catch (Exception ignored) {}
                throw new IOException("Failed to set isolation mode: " + responseBody);
            }
        }
    }
}

