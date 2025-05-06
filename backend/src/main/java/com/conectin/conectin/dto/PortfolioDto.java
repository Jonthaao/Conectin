package com.conectin.conectin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import com.conectin.conectin.entities.Portfolio;

@Data
public class PortfolioDto {
    private Integer id;
    private Integer prestadorId;
    private String titulo;
    private String descricao;
    private List<String> fotos;
    private LocalDateTime data;

    public static PortfolioDto fromEntity(Portfolio p) {
        PortfolioDto dto = new PortfolioDto();
        dto.setId(p.getId());
        dto.setPrestadorId(p.getPrestador() != null ? p.getPrestador().getId() : null);
        dto.setTitulo(p.getTitulo());
        dto.setDescricao(p.getDescricao());
        dto.setFotos(p.getFotos() != null ? p.getFotos() : Collections.emptyList());
        dto.setData(p.getData());
        return dto;
    }
}
