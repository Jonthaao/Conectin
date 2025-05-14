package com.conectin.conectin.repository; // ou .notifications

import com.conectin.conectin.entities.Notificacao;
import com.conectin.conectin.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Lista notificações para um usuário, ordenadas pela data de criação (mais recentes primeiro)
    List<Notificacao> findByDestinatarioOrderByDataCriacaoDesc(Usuario destinatario);

    // Conta notificações não lidas para um usuário
    long countByDestinatarioAndDataLeituraIsNull(Usuario destinatario);

    // Para marcar notificações como lidas em lote (exemplo)
    // @Modifying
    // @Query("UPDATE Notificacao n SET n.dataLeitura = CURRENT_TIMESTAMP WHERE n.destinatario = :destinatario AND n.id IN :ids")
    // void marcarComoLidas(@Param("destinatario") Usuario destinatario, @Param("ids") List<Long> ids);
}