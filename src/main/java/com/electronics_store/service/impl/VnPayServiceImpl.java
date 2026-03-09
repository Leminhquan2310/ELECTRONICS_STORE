package com.electronics_store.service.impl;

import com.electronics_store.config.VnPayConfig;
import com.electronics_store.model.Order;
import com.electronics_store.repository.OrderRepository;
import com.electronics_store.service.ExchangeRateService;
import com.electronics_store.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.electronics_store.config.VnPayConfig.hmacSHA512;

@Slf4j
@Service
public class VnPayServiceImpl implements VnPayService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ExchangeRateService exchangeRateService;

    @Override
    public String createPaymentUrl(HttpServletRequest request, long amount, String bankCode, long orderId) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(orderId);
        String vnp_IpAddr = VnPayConfig.getIpAddress(request);
        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);

        // QUAN TRỌNG: VNPay yêu cầu số tiền * 100
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Place order: " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn"); // Mặc định tiếng việt
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // 1. Build hash data (Phải dùng UTF-8)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                // 2. Build query (Phải dùng UTF-8)
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(VnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    @Override
    public int orderReturn(HttpServletRequest request) {
        // 1. Lấy tất cả parameter từ request
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                vnpParams.put(fieldName, fieldValue);
            }
        }
        // 2. Check sum
        String vnpSecureHash = vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");
        vnpParams.remove("vnp_SecureHash");

        String signValue = VnPayConfig.hashAllFields(vnpParams);

        if (!signValue.equals(vnpSecureHash)) {
            return -1;
        }

        // 3. check Amount
        Order order = orderRepository.findById(Long.parseLong(vnpParams.get("vnp_TxnRef"))).orElseThrow(() -> new RuntimeException("Order not found"));
        long amount = Long.parseLong(vnpParams.get("vnp_Amount"));
        String cardType = request.getParameter("vnp_CardType");
        if (!checkAmount(cardType, amount, order.getTotalAmount())) {
            return 0;
        }


        // 7. Kiểm tra trạng thái giao dịch
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return 1; // Thành công
        }

        return 0; // Thất bại
    }


    private boolean checkAmount(String cardType, long vnp_Amount, BigDecimal orderTotalAmount) {
        if ("ATM".equalsIgnoreCase(cardType)) {
            BigDecimal currencyVND = exchangeRateService.usdToVnd();
            BigDecimal orderAmountVnd = orderTotalAmount.multiply(currencyVND);
            long expectedAmount = orderAmountVnd.setScale(0, RoundingMode.HALF_UP).longValue();
            return vnp_Amount == (expectedAmount * 100);
        } else {
            return vnp_Amount == orderTotalAmount.setScale(0, RoundingMode.HALF_UP).longValue() ;
        }
    }
}
