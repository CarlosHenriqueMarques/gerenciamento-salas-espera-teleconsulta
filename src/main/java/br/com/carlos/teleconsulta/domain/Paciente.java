package br.com.carlos.teleconsulta.domain;

import br.com.carlos.teleconsulta.domain.enums.Sexo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "paciente")
public class Paciente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 120)
    private String nomeSocial;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Sexo sexo;

    @Size(max = 120)
    private String nomeMae;

    @Size(max = 120)
    private String nomePai;

    @Size(max = 20)
    private String telefone;

    @Email @Size(max = 150)
    private String email;

    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    @Column(length = 11)
    private String cpf;

    @Size(max = 20)
    private String rg;

    @Pattern(regexp = "\\d{15}", message = "CNS deve conter 15 dígitos")
    @Column(length = 15)
    private String cns;

    @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
    private LocalDate dataNascimento;

    @Size(max = 255)
    private String endereco;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNomeSocial() { return nomeSocial; }
    public void setNomeSocial(String nomeSocial) { this.nomeSocial = nomeSocial; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }

    public String getNomePai() { return nomePai; }
    public void setNomePai(String nomePai) { this.nomePai = nomePai; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getCns() { return cns; }
    public void setCns(String cns) { this.cns = cns; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}
