package br.com.petcare.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "PC_PET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 40)
    private String raca;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "foto_url", length = 300)
    private String fotoUrl;

    // O tutor dono do pet - cada pet pertence a um usuário do tipo TUTOR
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Usuario tutor;
}