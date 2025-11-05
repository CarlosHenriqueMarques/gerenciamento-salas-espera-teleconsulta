package br.com.carlos.teleconsulta.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sala")
public class Sala implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 120, nullable = false)
    private String nome;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer capacidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", nullable = false)
    private UnidadeSaude unidadeSaude;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) dataCadastro = LocalDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }

    public UnidadeSaude getUnidadeSaude() { return unidadeSaude; }
    public void setUnidadeSaude(UnidadeSaude unidadeSaude) { this.unidadeSaude = unidadeSaude; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
