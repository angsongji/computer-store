package dao;

import com.sun.jdi.connect.spi.Connection;
import dto.NhaCungCapDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NhaCungCapDAO {

    private MySQLConnect db = new MySQLConnect();

    public ArrayList<NhaCungCapDTO> list() {
        ArrayList<NhaCungCapDTO> nccList = new ArrayList<>();

        try {
            String sql = "SELECT * FROM nhacungcap";
            ResultSet rs = db.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString("id");
                String ten = rs.getString("ten");
                String dia_chi = rs.getString("dia_chi");
                String sdt = rs.getString("sdt");
                boolean enable = rs.getBoolean("enable");

                NhaCungCapDTO nhaCungCap = new NhaCungCapDTO(id, ten, dia_chi, sdt, enable);
                nccList.add(nhaCungCap);
            }
            rs.close();
            db.disConnect();
        } catch (SQLException ex) {
            Logger.getLogger(NhaCungCapDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nccList;
    }

    public void updateDB(NhaCungCapDTO ncc) {
        String sql = "UPDATE nhacungcap SET ";
        sql += "ten='" + ncc.getTenNhaCungCap() + "', ";
        sql += "dia_chi='" + ncc.getDiachi() + "', ";
        sql += "sdt='" + ncc.getSdt() + "' ";
        sql += "WHERE id='" + ncc.getIdNhaCungCap() + "'";
        db.executeUpdate(sql);
    }
    public void updateAddress(String idNhaCungCap, String diaChiMoi) {
    String sql = "UPDATE nhacungcap SET dia_chi='" + diaChiMoi + "' WHERE id='" + idNhaCungCap + "'";
    db.executeUpdate(sql);
}

    public void addDB(NhaCungCapDTO ncc) {
        String sql = "INSERT INTO nhacungcap VALUES (";
        sql += "'" + ncc.getIdNhaCungCap() + "', ";
        sql += "N'" + ncc.getTenNhaCungCap() + "', ";
        sql += "N'" + ncc.getDiachi() + "', ";
        sql += "'" + ncc.getSdt() + "', ";
        sql += "'1')";
        db.executeUpdate(sql);
    }

    public String getLastMaNCC() {
        String sql = "SELECT id FROM nhacungcap ORDER BY id DESC LIMIT 1";
        try {
            ResultSet rs = db.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không có mã nào
    }

    public String generateNewMaNCC() {
        String lastMaNCC = getLastMaNCC();
        if (lastMaNCC != null && lastMaNCC.startsWith("CC")) {
            // Lấy phần số từ mã NCC cuối cùng, tăng lên 1
            int lastNumber = Integer.parseInt(lastMaNCC.substring(2));
            int newNumber = lastNumber + 1;
            // Định dạng lại mã với 3 chữ số
            return "CC" + String.format("%03d", newNumber);
        }
        // Nếu không có mã nào, bắt đầu từ CC001
        return "CC001";
    }

    public void create_ncc(NhaCungCapDTO ncc) {
        String newMaNCC = generateNewMaNCC(); // Tạo mã NCC mới
        String sql = "INSERT INTO nhacungcap (id, ten, dia_chi, sdt, enable) VALUES ("
                + "'" + newMaNCC + "', "
                + "N'" + ncc.getTenNhaCungCap() + "', "
                + "N'" + ncc.getDiachi() + "', "
                + "'" + ncc.getSdt() + "', "
                + "'1')";
        db.executeUpdate(sql);
    }

    public void deleteDB(String id) {
        String sql = "UPDATE nhacungcap SET enable = 0 WHERE id='" + id + "'";
        db.executeUpdate(sql);
    }
    public boolean deleteDB_by_tiep(String idNhaCungCap) {
    // Kiểm tra nếu ID không tồn tại trong bảng nhacungcap
    if (!checkExists(idNhaCungCap)) {
        throw new IllegalArgumentException("ID nhà cung cấp không tồn tại trong cơ sở dữ liệu.");
    }

    // Kiểm tra nếu ID đã được tham chiếu trong bảng phieu_nhap
    if (checkReferencedInPhieuNhap(idNhaCungCap)) {
        throw new IllegalStateException("ID nhà cung cấp đã được sử dụng trong bảng phiếu nhập, không thể xóa.");
    }

    // Xây dựng câu lệnh SQL trực tiếp
    String sql = "UPDATE nhacungcap SET enable = 0 WHERE id = '" + idNhaCungCap + "'";
    db.executeUpdate(sql);
    return true; // Xóa thành công
    
}

    public boolean checkExists(String idNhaCungCap) {
    String sql = "SELECT COUNT(*) FROM nhacungcap WHERE id = '" + idNhaCungCap + "'";
    try {
        ResultSet rs = db.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt(1) > 0; // Trả về true nếu ID tồn tại
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false; 
}

    public boolean checkReferencedInPhieuNhap(String idNhaCungCap) {
    String sql = "SELECT COUNT(*) FROM phieunhap WHERE id_ncc = '" + idNhaCungCap + "'";
    try {
        ResultSet rs = db.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt(1) > 0; // Trả về true nếu có ít nhất 1 bản ghi
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}



}