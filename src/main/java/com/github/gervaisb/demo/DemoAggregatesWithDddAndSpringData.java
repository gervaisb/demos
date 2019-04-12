package com.github.gervaisb.demo;

import com.github.gervaisb.demo.product.Product;
import com.github.gervaisb.demo.product.ProductOwnerId;
import com.github.gervaisb.demo.product.ProductsService;
import com.github.gervaisb.demo.tenant.TenantId;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoAggregatesWithDddAndSpringData implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoAggregatesWithDddAndSpringData.class, args);
	}

	private final ProductsService service;

	public DemoAggregatesWithDddAndSpringData(ProductsService service) {
		this.service = service;
	}

	@Override
	public void run(String... args) {
		TenantId tenantId = new TenantId();
		Product product;

		product = service.create(tenantId, new ProductOwnerId(), "Test", "A product to test");

		product = service.get(tenantId, product.getProductId());
		System.out.println(product);
	}
}
