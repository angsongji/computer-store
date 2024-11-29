/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package API;

/**
 *
 * @author hp
 */
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.ArrayList;
import dto.PhieuNhapDTO;
import dao.PhieuNhapDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ServerSocket;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dao.BaoHanhDAO;
import dao.CTPhieuNhapDAO;
import dao.NhaCungCapDAO;
import dto.CTPhieuNhapDTO;
import dto.BaoHanhDTO;
import dto.NhaCungCapDTO;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class API_Server extends NanoHTTPD {

    public API_Server(int port) throws IOException {
        super(port);

        // Kiểm tra xem cổng đã được sử dụng chưa
        if (isPortInUse(port)) {

        } else {
            start(SOCKET_READ_TIMEOUT, false); // Khởi chạy server
            System.out.println("API server started on port " + port);
        }

    }

    // Kiểm tra cổng có đang sử dụng không
    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false; // Cổng chưa bị chiếm dụng
        } catch (IOException e) {
            return true; // Cổng đã bị chiếm dụng
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String method = session.getMethod().name();
        ObjectMapper objectMapper = new ObjectMapper();

        switch (uri) {
            case "/api/get-suppliers":
                if ("GET".equals(method)) {

                    return handleGetNhaCungCap(session, objectMapper);
                }
                break;

            case "/api/create-suppliers":
                if ("POST".equals(method)) {
                    return handleCreateNhaCungCap(session, objectMapper);
                }
                break;

            case "/api/update-suppliers":
                if ("PUT".equals(method)) {

                    return handleUpdateNhaCungCap(session, objectMapper);
                }
                break;
            case "/api/delete-suppliers":
                if ("DELETE".equals(method)) {
                    return handleDeleteNhaCungCap(session, objectMapper);
                }
                break;
            default:
                return newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "application/json",
                        "{\"status\": 404, \"message\": \"Không tìm thấy API\"}"
                );

        }
        return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                "application/json",
                "{\"status\": 405, \"message\": \"Reponse cho phương thức chưa được thiết lập\"}"
        );

    }
    Map<String, Object> response = new HashMap<>();

    private Response handleGetNhaCungCap(IHTTPSession session, ObjectMapper objectMapper) {
        try {
            NhaCungCapDAO ncc = new NhaCungCapDAO();
            ArrayList<NhaCungCapDTO> nccList = ncc.list();
            // Lấy tham số từ query

            String idNhaCungCap = session.getParms().get("idNhaCungCap");

            // Lọc danh sách theo điều kiện
            if (idNhaCungCap != null && !idNhaCungCap.isEmpty()) {
                nccList.removeIf(nccItem -> !nccItem.getIdNhaCungCap().equals(idNhaCungCap));
            }
            HashMap<String, Object> response = new HashMap<>();
            response.put("data", nccList);
            response.put("status", 200);
            if (nccList.isEmpty()) {
                response.put("message", "Không có dữ liệu phù hợp");
            } else {
                response.put("message", "Lấy danh sách nhà cung cấp thành công");
            }
            // Chuyển danh sách thành JSON
            String jsonResponse = objectMapper.writeValueAsString(response);
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 400);
            errorResponse.put("message", "Bad request");
            try {
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", errorJson);
            } catch (IOException ioException) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
            }
        }
    }
// Xử lý thêm nhà cung cấp

    private Response handleCreateNhaCungCap(IHTTPSession session, ObjectMapper objectMapper) {
        try {
            // Đọc dữ liệu từ body
            Map<String, String> body = new HashMap<>();
            session.parseBody(body);
            String payload = body.get("postData");
            NhaCungCapDTO newSupplier = objectMapper.readValue(payload, NhaCungCapDTO.class);

            // Kiểm tra dữ liệu đầu vào
            if (newSupplier.getTenNhaCungCap() == null || newSupplier.getTenNhaCungCap().isEmpty() || newSupplier.getSdt().isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"status\": 400, \"message\": \"Tên nhà cung cấp không được để trống\"}");
            }   // thêm kiểm tra điều kiện nếu cần, tại tui thấy cần chụp có 1 trường hợp lỗi thôi nên bắt có 1 lỗi, nếu cần thiết thì làm kĩ hơn
            NhaCungCapDAO nccDAO = new NhaCungCapDAO();
            nccDAO.create_ncc(newSupplier);
            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\": 200, \"message\": \"Thêm nhà cung cấp thành công\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"status\": 500, \"message\": \"Đã xảy ra lỗi\"}");
        }
    }

// Xử lý sửa nhà cung cấp má nó chứ nó k tự động đọc payload như bên post
 private Response handleUpdateNhaCungCap(IHTTPSession session, ObjectMapper objectMapper) {
    try {
        // Lấy độ dài body từ request header (nếu có)
        int contentLength = Integer.parseInt(session.getHeaders().getOrDefault("content-length", "0"));
        if (contentLength <= 0) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"Không có dữ liệu\"}");
        }

        // Đọc body từ InputStream với giới hạn dung lượng
        byte[] buffer = new byte[contentLength];
        int read = session.getInputStream().read(buffer, 0, contentLength);
        if (read <= 0) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"Không đọc được dữ liệu\"}");
        }

        // Chuyển buffer thành chuỗi payload
        String payload = new String(buffer, 0, read);
        System.out.println("Payload: " + payload);

        // Parse payload từ JSON thành Map
        Map<String, String> requestData = objectMapper.readValue(payload, HashMap.class);

        // Lấy dữ liệu từ payload
        String idNhaCungCap = requestData.get("idNhaCungCap");
        String diaChiMoi = requestData.get("diaChi");

        // Kiểm tra dữ liệu đầu vào
        if (idNhaCungCap == null || idNhaCungCap.isEmpty()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"ID nhà cung cấp không được để trống\"}");
        }
        if (diaChiMoi == null || diaChiMoi.isEmpty()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"Địa chỉ mới không được để trống\"}");
        }

        // Cập nhật database
        NhaCungCapDAO nccDAO = new NhaCungCapDAO();
        nccDAO.updateAddress(idNhaCungCap, diaChiMoi);

        return newFixedLengthResponse(Response.Status.OK, "application/json",
            "{\"status\": 200, \"message\": \"Cập nhật địa chỉ thành công\"}");
    } catch (Exception e) {
        e.printStackTrace();
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
            "{\"status\": 500, \"message\": \"Đã xảy ra lỗi\"}");
    }
}
// Xử lý xóa nhà cung cấp
    private Response handleDeleteNhaCungCap(IHTTPSession session, ObjectMapper objectMapper) {
    try {
        // Lấy tham số id từ query
        String idNhaCungCap = session.getParms().get("id");
        if (idNhaCungCap == null || idNhaCungCap.isEmpty()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"ID nhà cung cấp không được để trống\"}");
        }

        NhaCungCapDAO nccDAO = new NhaCungCapDAO();

        try {
            // Gọi hàm xóa
            boolean success = nccDAO.deleteDB_by_tiep(idNhaCungCap);
            if (success) {
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    "{\"status\": 200, \"message\": \"Xóa nhà cung cấp thành công\"}");
            } else {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"status\": 500, \"message\": \"Xóa thất bại do lỗi hệ thống\"}");
            }
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi ID không tồn tại
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"" + e.getMessage() + "\"}");
        } catch (IllegalStateException e) {
            // Xử lý lỗi ID đã được tham chiếu trong phiếu nhập
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"status\": 400, \"message\": \"" + e.getMessage() + "\"}");
        }

    } catch (Exception e) {
        e.printStackTrace();
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
            "{\"status\": 500, \"message\": \"Đã xảy ra lỗi\"}");
    }
}


}