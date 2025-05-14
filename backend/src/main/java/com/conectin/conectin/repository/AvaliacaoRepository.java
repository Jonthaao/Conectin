package com.conectin.conectin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.Usuario;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    List<Avaliacao> findByAvaliadoId(Long avaliadoId); // Alterado para Long para ser consistente com Usuario.id

    // Calcula a média de notas para um usuário QUANDO ELE FOI AVALIADO COMO PRESTADOR
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.avaliado.id = :usuarioId AND a.solicitacao.prestador.id = :usuarioId")
    Float calcularMediaNotasParaUsuarioComoPrestador(@Param("usuarioId") Long usuarioId);

    // Calcula a média de notas para um usuário QUANDO ELE FOI AVALIADO COMO CLIENTE
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.avaliado.id = :usuarioId AND a.solicitacao.cliente.id = :usuarioId")
    Float calcularMediaNotasParaUsuarioComoCliente(@Param("usuarioId") Long usuarioId);

    // Para listar avaliações recebidas por um usuário como prestador
    @Query("SELECT a FROM Avaliacao a WHERE a.avaliado.id = :usuarioId AND a.solicitacao.prestador.id = :usuarioId")
    List<Avaliacao> findAvaliacoesRecebidasComoPrestador(@Param("usuarioId") Long usuarioId);

    // Para listar avaliações recebidas por um usuário como cliente
    @Query("SELECT a FROM Avaliacao a WHERE a.avaliado.id = :usuarioId AND a.solicitacao.cliente.id = :usuarioId")
    List<Avaliacao> findAvaliacoesRecebidasComoCliente(@Param("usuarioId") Long usuarioId);

    List<Avaliacao> findBySolicitacaoAndAvaliador(SolicitacaoServico solicitacao, Usuario avaliador);
}