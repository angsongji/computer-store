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
import dto.CTPhieuNhapDTO;
import dto.BaoHanhDTO;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
            case "/api/list-phieu-nhap":
                if ("GET".equals(method)) {
                    objectMapper.registerModule(new JavaTimeModule());
                    return handleGetPhieuNhap(objectMapper);
                }
                break;

            case "/api/list-ctphieu-nhap":
                if ("GET".equals(method)) {
                    return handleGetCTPhieuNhap(objectMapper);
                }
                break;

            case "/api/bao-hanh":
                if ("GET".equals(method)) {
                    objectMapper.registerModule(new JavaTimeModule());
                    return handleGetBaoHanh(session, objectMapper);
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
    private Response handleGetPhieuNhap(ObjectMapper objectMapper) {
        try {
            PhieuNhapDAO pnDAO = new PhieuNhapDAO();
            ArrayList<PhieuNhapDTO> pnList = pnDAO.list();

            response.put("data", pnList);
            response.put("status", 200);
            response.put("message", "Lấy danh sách phiếu nhập thành công");
            String jsonResponse = objectMapper.writeValueAsString(response);

            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        } catch (IOException e) {
            return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    "{\"status\": 500, \"message\": \"Lỗi trả về danh sách phiếu nhập\"}"
            );

        }
    }

    private Response handleGetCTPhieuNhap(ObjectMapper objectMapper) {
        CTPhieuNhapDAO ctpnDAO = new CTPhieuNhapDAO();

        try {
            ArrayList<CTPhieuNhapDTO> ctpnList = ctpnDAO.list();
            String jsonResponse = "";
            jsonResponse = objectMapper.writeValueAsString(ctpnList);
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
        }

    }

    private Response handleGetBaoHanh(IHTTPSession session, ObjectMapper objectMapper) {
    try {
        BaoHanhDAO pbhDAO = new BaoHanhDAO();
        ArrayList<BaoHanhDTO> pbhList = pbhDAO.list();
        // Lấy tham số từ query
        LocalDate ngayBaoHanh = parseDateParam(session.getParms().get("ngayBaoHanh"));
        LocalDate ngayTraMay = parseDateParam(session.getParms().get("ngayTraMay"));
        String idBaoHanh = session.getParms().get("idBaoHanh");
        String idKhachHang = session.getParms().get("idKhachHang");

        // Lọc danh sách theo điều kiện
        pbhList.removeIf(pbh
                -> (idBaoHanh != null && !idBaoHanh.isEmpty() && !pbh.getIdBaoHanh().equals(idBaoHanh))
                || (idKhachHang != null && !idKhachHang.isEmpty() && !pbh.getIdKhachHang().equals(idKhachHang))
                || (ngayBaoHanh != null && !pbh.getNgayBaoHanh().equals(ngayBaoHanh))
                || (ngayTraMay != null && !pbh.getNgayTraMay().equals(ngayTraMay))
        );
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", pbhList);
        response.put("status", 200);
        if (pbhList.isEmpty()) {
            response.put("message", "Không có dữ liệu phù hợp");
        } else {
            response.put("message", "Lấy danh sách bảo hành thành công");
        }
        // Chuyển danh sách thành JSON
        String jsonResponse = objectMapper.writeValueAsString(response);
        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("aaaaaaaa");
        // Trả về lỗi 500 và thông tin chi tiết về lỗi trong response
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 500);
        errorResponse.put("message", e.getMessage()); // Xuất thông tin lỗi chi tiết
        try {
            String errorJson = objectMapper.writeValueAsString(errorResponse);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", errorJson);
        } catch (IOException ioException) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
        }
    }
}

    private LocalDate parseDateParam(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr); // Định dạng: yyyy-MM-dd
    }

}
