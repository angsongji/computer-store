/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.BaoHanhDTO;
import dto.PhieuNhapDTO;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class BaoHanhController {
    public List<BaoHanhDTO> getBaoHanh() { 
    List<BaoHanhDTO> BaoHanhList = null;
    try {
        // URL của API
        String apiUrl = "http://localhost:8080/api/bao-hanh"; // Thay đổi port nếu cần

        // Tạo kết nối HTTP
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        // Kiểm tra phản hồi từ API
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
        }

        // Đọc phản hồi JSON từ API
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        // Chuyển chuỗi JSON thành đối tượng Map (để lấy dữ liệu từ phần "data")
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Đăng ký module để hỗ trợ LocalDate
        Map<String, Object> responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});

        // Lấy danh sách từ phần "data" trong JSON
        BaoHanhList = objectMapper.convertValue(responseMap.get("data"), new TypeReference<List<BaoHanhDTO>>() {});

    } catch (Exception e) {
        e.printStackTrace();
    }
    return BaoHanhList;
}
}
