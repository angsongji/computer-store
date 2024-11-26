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
import dao.CTPhieuNhapDAO;
import dto.CTPhieuNhapDTO;
import dto.PhieuBaoHanhDTO;
import dao.PhieuBaoHanhDAO;
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
        try ( ServerSocket serverSocket = new ServerSocket(port)) {
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

            case "/api/list-bao-hanh":
                if ("GET".equals(method)) {
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

    private Response handleGetPhieuNhap(ObjectMapper objectMapper) {
        try {
            PhieuNhapDAO pnDAO = new PhieuNhapDAO();
            ArrayList<PhieuNhapDTO> pnList = pnDAO.list();
            Map<String, Object> response = new HashMap<>();
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
        try {
            CTPhieuNhapDAO ctpnDAO = new CTPhieuNhapDAO();
            ArrayList<CTPhieuNhapDTO> ctpnList = ctpnDAO.list();
            Map<String, Object> response = new HashMap<>();
            response.put("data", ctpnList);
            response.put("status", 200);
            response.put("message", "Lấy danh sách chi tiết phiếu nhập thành công");
            String jsonResponse = objectMapper.writeValueAsString(response);
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        } catch (IOException e) {
            return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    "{\"status\": 500, \"message\": \"Lỗi trả về danh sách chi tiết phiếu nhập\"}"
            );
        }

    }

    private Response handleGetBaoHanh(IHTTPSession session, ObjectMapper objectMapper) {
        try {
            PhieuBaoHanhDAO pbhDAO = new PhieuBaoHanhDAO();
            ArrayList<PhieuBaoHanhDTO> pbhList = pbhDAO.list();

            // Lấy tham số từ query
            LocalDate ngayMua = parseDateParam(session.getParms().get("ngayMua"));
            LocalDate ngayHetHan = parseDateParam(session.getParms().get("ngayHetHan"));
            String idHoaDon = session.getParms().get("idHoaDon");
            String idKhachHang = session.getParms().get("idKhachHang");

            // Lọc danh sách theo điều kiện
            pbhList.removeIf(pbh
                    -> (idHoaDon != null && !idHoaDon.isEmpty() && !pbh.getIdHoaDon().equals(idHoaDon))
                    || (idKhachHang != null && !idKhachHang.isEmpty() && !pbh.getIdKhachHang().equals(idKhachHang))
                    || (ngayMua != null && !pbh.getNgayMua().equals(ngayMua))
                    || (ngayHetHan != null && !pbh.getNgayHetHan().equals(ngayHetHan))
            );

            // Chuyển danh sách thành JSON
            String jsonResponse = objectMapper.writeValueAsString(pbhList);
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
        }
    }

    private LocalDate parseDateParam(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr); // Định dạng: yyyy-MM-dd
    }

}
