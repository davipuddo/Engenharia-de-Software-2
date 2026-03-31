package com.exemplo;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SchemaGenerator {
    public static void main(String[] args) {
        System.out.println("Iniciando geração de schema do Hibernate (Offline)...");
        try (SessionFactory sessionFactory = new Configuration().configure("hibernate-export.cfg.xml").buildSessionFactory()) {
            System.out.println("Script gerado com sucesso na raiz do projeto como 'hibernate_mysql_script.sql'.");
        } catch (Exception e) {
            System.err.println("Erro ao gerar o script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
