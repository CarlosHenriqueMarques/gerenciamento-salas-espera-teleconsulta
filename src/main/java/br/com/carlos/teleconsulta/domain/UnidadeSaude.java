package br.com.carlos.teleconsulta.domain;

import br.com.carlos.teleconsulta.validation.CNPJ;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "unidade_saude",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_unidade_cnpj", columnNames = "cnpj"),
                @UniqueConstraint(name = "uk_unidade_cnes", columnNames = "cnes")
        }
)
public class UnidadeSaude implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank @Size(max = 150)
    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String sigla;

    @NotBlank @Size(min = 14, max = 14)
    @CNPJ
    @Column(nullable = false, length = 14)
    private String cnpj;

    @NotBlank @Size(min = 7, max = 7)
    @Column(nullable = false, length = 7)
    private String cnes;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getCnes() { return cnes; }
    public void setCnes(String cnes) { this.cnes = cnes; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
