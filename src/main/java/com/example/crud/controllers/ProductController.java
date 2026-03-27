package com.example.crud.controllers;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.domain.category.RequestCategory;
import com.example.crud.domain.product.RequestProduct;
import com.example.crud.service.AddressSearch;
import com.example.crud.service.ViaCepService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    private final AddressSearch addressSearch;
    private final ViaCepService viaCepService;

    @Autowired
    public ProductController(ProductRepository repository,
                             AddressSearch addressSearch,
                             ViaCepService viaCepService) {
        this.repository = repository;
        this.addressSearch = addressSearch;
        this.viaCepService = viaCepService;
    }

    @GetMapping
    public ResponseEntity getAllProducts() {
        var allProducts = repository.findAllByActiveTrue();
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/cep")
    public ResponseEntity verifyAvailability(@RequestParam String state,
                                             @RequestParam String city,
                                             @RequestParam String street) {
        String cep = addressSearch.searchAddress(state, city, street);
        return ResponseEntity.ok(cep);
    }

    /**
     * Novo endpoint — verifica disponibilidade do produto por CEP.
     *
     * GET /product/availability?productId=p1&cep=08773380
     *
     * Retorna:
     *   { "available": true }  — produto disponível na cidade do CEP
     *   { "available": false } — produto não disponível na cidade do CEP
     */
    @GetMapping("/availability")
    public ResponseEntity<?> checkProductAvailability(@RequestParam String productId,
                                                      @RequestParam String cep) {
        boolean available = viaCepService.checkAvailability(productId, cep);
        return ResponseEntity.ok(new AvailabilityResponse(available));
    }

    // DTO interno para a resposta
    record AvailabilityResponse(boolean available) {}

    @GetMapping("/endpoint1")
    public ResponseEntity getAllProducts1(@RequestParam String categoryAsParam) {
        var allProducts = repository.findAllByCategory(categoryAsParam);
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/endpoint2/{id}")
    public ResponseEntity getProduct(@PathVariable String id) {
        Optional<Product> optionalProduct = repository.findById(id);
        return ResponseEntity.ok(optionalProduct);
    }

    @GetMapping("/endpoint3/top5byprice")
    public ResponseEntity getAllProducts3() {
        var allProducts = repository.findAllByActiveTrue();
        List<Product> topFive = allProducts
                .stream()
                .sorted(Comparator.comparingInt(Product::getPrice).reversed())
                .limit(5)
                .collect(Collectors.toList());
        return ResponseEntity.ok(topFive);
    }

    @GetMapping("/category/{categoryAsPath}")
    public ResponseEntity getProductsByCategory(
            @RequestHeader String categoryAsHeader,
            @PathVariable String categoryAsPath,
            @RequestBody @Valid RequestCategory categoryAsBody,
            @RequestParam String categoryAsParam) {
        var allProducts = repository.findAllByActiveTrue();
        List<Product> filteredProducts = new ArrayList<>();
        for (Product product : allProducts) {
            if (categoryAsParam.equals(product.getCategory())) {
                filteredProducts.add(product);
            }
        }
        return ResponseEntity.ok(filteredProducts);
    }

    @PostMapping
    public ResponseEntity registerProduct(@RequestBody @Valid RequestProduct data) {
        Product newProduct = new Product(data);
        repository.save(newProduct);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Transactional
    public ResponseEntity updateProduct(@RequestBody @Valid RequestProduct data) {
        Optional<Product> optionalProduct = repository.findById(data.id());
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(data.name());
            product.setPrice(data.price());
            return ResponseEntity.ok(product);
        } else {
            throw new EntityNotFoundException();
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteProduct(@PathVariable String id) {
        Optional<Product> optionalProduct = repository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setActive(false);
            return ResponseEntity.noContent().build();
        } else {
            throw new EntityNotFoundException();
        }
    }
}
