package com.example.productservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final List<Map<String, Object>> products = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(11);

    public ProductController() {
        products.add(createProduct(1, "Mouse", 150000));
        products.add(createProduct(2, "Keyboard", 250000));
        products.add(createProduct(3, "Monitor", 2500000));
        products.add(createProduct(4, "Laptop Stand", 320000));
        products.add(createProduct(5, "USB-C Hub", 450000));
        products.add(createProduct(6, "Webcam HD", 680000));
        products.add(createProduct(7, "Headset Gaming", 890000));
        products.add(createProduct(8, "Mouse Pad XL", 120000));
        products.add(createProduct(9, "External SSD 1TB", 2150000));
        products.add(createProduct(10, "Mechanical Switches", 350000));
    }

    private Map<String, Object> createProduct(int id, String name, double price) {
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", id);
        product.put("name", name);
        if (price == Math.floor(price)) {
            product.put("price", (long) price);
        } else {
            product.put("price", price);
        }
        return product;
    }

    @GetMapping
    public synchronized List<Map<String, Object>> getProducts(
            @RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>(products);
        }
        String keyword = name.trim().toLowerCase(Locale.ROOT);
        return products.stream()
                .filter(p -> String.valueOf(p.get("name"))
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword))
                .toList();
    }

    @GetMapping("/search")
    public synchronized List<Map<String, Object>> searchProducts(
            @RequestParam(defaultValue = "") String name) {
        return getProducts(name);
    }

    @GetMapping("/{id}")
    public synchronized ResponseEntity<Map<String, Object>> getProduct(@PathVariable int id) {
        return products.stream()
                .filter(p -> p.get("id").equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public synchronized ResponseEntity<?> addProduct(@RequestBody Map<String, Object> payload) {
        Object rawName = payload.get("name");
        Object rawPrice = payload.get("price");

        if (rawName == null || String.valueOf(rawName).trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tên sản phẩm không được để trống"));
        }

        double price;
        try {
            if (rawPrice == null) {
                throw new NumberFormatException("Missing price");
            }
            price = Double.parseDouble(String.valueOf(rawPrice));
            if (price < 0 || Double.isNaN(price) || Double.isInfinite(price)) {
                throw new NumberFormatException("Negative price");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Giá sản phẩm phải là số không âm hợp lệ"));
        }

        int id = nextId.getAndIncrement();
        Map<String, Object> newProduct = createProduct(id, String.valueOf(rawName).trim(), price);
        products.add(newProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<?> deleteProduct(@PathVariable int id) {
        boolean removed = products.removeIf(p -> p.get("id").equals(id));
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm thành công", "id", id));
    }
}
