package com.example.kasiria.data;

import java.util.List;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class DocumenteroPostData {

    @SerializedName("document")
    @Expose
    private String document;

    @SerializedName("apiKey")
    @Expose
    private String apiKey;

    @SerializedName("format")
    @Expose
    private String format;

    @SerializedName("data")
    @Expose
    private Data data;

    public static class Data {

        @SerializedName("businessName")
        @Expose
        private String businessName;

        @SerializedName("businessAddress")
        @Expose
        private String businessAddress;

        @SerializedName("orderId")
        @Expose
        private String orderId;

        @SerializedName("createdAt")
        @Expose
        private String createdAt;

        @SerializedName("products")
        @Expose
        private List<Product> products;

        @SerializedName("subtotal")
        @Expose
        private String subtotal;

        @SerializedName("phoneNo")
        @Expose
        private String phoneNo;

        @SerializedName("userName")
        @Expose
        private String userName;

        public String getBusinessName() {
            return businessName;
        }

        public void setBusinessName(String businessName) {
            this.businessName = businessName;
        }

        public String getBusinessAddress() {
            return businessAddress;
        }

        public void setBusinessAddress(String businessAddress) {
            this.businessAddress = businessAddress;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(String subtotal) {
            this.subtotal = subtotal;
        }

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public static class Product {

        @SerializedName("productName")
        @Expose
        private String productName;

        @SerializedName("productQuantity")
        @Expose
        private String productQuantity;

        @SerializedName("productPrice")
        @Expose
        private String productPrice;

        @SerializedName("productTotal")
        @Expose
        private String productTotal;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductQuantity() {
            return productQuantity;
        }

        public void setProductQuantity(String productQuantity) {
            this.productQuantity = productQuantity;
        }

        public String getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(String productPrice) {
            this.productPrice = productPrice;
        }

        public String getProductTotal() {
            return productTotal;
        }

        public void setProductTotal(String productTotal) {
            this.productTotal = productTotal;
        }
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
