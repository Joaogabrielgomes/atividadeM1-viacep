package com.example.crud.service;

import com.example.crud.domain.address.Address;
import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class ViaCepService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    public ViaCepService(RestTemplate restTemplate,
                         ObjectMapper objectMapper,
                         ProductRepository productRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
    }

    /**
     * Busca o endereço pelo CEP na API ViaCEP e retorna o objeto Address.
     */
    public Address searchByCep(String cep) {
        String url = "https://viacep.com.br/ws/{cep}/json/";
        ResponseEntity<Address> response = restTemplate.getForEntity(url, Address.class, cep);
        return response.getBody();
    }

    /**
     * Verifica se o produto está disponível na cidade correspondente ao CEP informado.
     *
     * @param productId ID do produto
     * @param cep       CEP a ser consultado
     * @return true se a cidade do CEP for igual ao distribution_center do produto; false caso contrário
     */
    public boolean checkAvailability(String productId, String cep) {
        // Busca o produto no banco
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            throw new EntityNotFoundException("Produto não encontrado com id: " + productId);
        }
        Product product = optionalProduct.get();

        // Consulta o ViaCEP
        Address address = searchByCep(cep);
        if (address == null || address.getLocalidade() == null) {
            throw new RuntimeException("CEP inválido ou não encontrado: " + cep);
        }

        // Comparação case-insensitive entre a cidade do CEP e o centro de distribuição do produto
        String cidadeCep = address.getLocalidade().trim();
        String distributionCenter = product.getDistributionCenter() != null
                ? product.getDistributionCenter().trim()
                : "";

        return cidadeCep.equalsIgnoreCase(distributionCenter);
    }
}
