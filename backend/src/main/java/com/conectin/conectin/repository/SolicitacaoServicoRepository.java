package com.conectin.conectin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query; // Se for usar query customizada
// import org.springframework.data.repository.query.Param; // Se for usar query customizada

import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.StatusSolicitacao;

public interface SolicitacaoServicoRepository extends JpaRepository<SolicitacaoServico, Integer> {

    List<SolicitacaoServico> findByPrestadorIdAndStatusOrderByDataHoraSolicitacaoDesc(Long prestadorId, StatusSolicitacao status);
    List<SolicitacaoServico> findByPrestadorIdOrderByDataHoraSolicitacaoDesc(Long prestadorId);
    List<SolicitacaoServico> findByClienteIdAndStatusOrderByDataHoraSolicitacaoDesc(Long clienteId, StatusSolicitacao status);
    List<SolicitacaoServico> findByClienteIdOrderByDataHoraSolicitacaoDesc(Long clienteId);
    
    // NOVO MÉTODO: para verificar duplicidade na criação
    List<SolicitacaoServico> findByClienteIdAndPrestadorIdAndStatusOrderByDataHoraSolicitacaoDesc(
        Long clienteId, Long prestadorId, StatusSolicitacao status);
    List<SolicitacaoServico> findByClienteIdAndPrestadorIdAndStatus(Long clienteId, Long prestadorId, String string);
}