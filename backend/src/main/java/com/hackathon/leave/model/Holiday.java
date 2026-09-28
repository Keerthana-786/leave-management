package com.hackathon.leave.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private String name;

    public Holiday() {
    }

    public Holiday(Long id, LocalDate date, String name) {
        this.id = id;
        this.date = date;
        this.name = name;
    }

    public static HolidayBuilder builder() {
        return new HolidayBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class HolidayBuilder {
        private Long id;
        private LocalDate date;
        private String name;

        public HolidayBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public HolidayBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public HolidayBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Holiday build() {
            return new Holiday(id, date, name);
        }
    }
}
