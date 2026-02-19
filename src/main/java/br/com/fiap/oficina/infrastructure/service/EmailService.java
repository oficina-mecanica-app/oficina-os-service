package br.com.fiap.oficina.infrastructure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.fiap.oficina.infrastructure.persistence.entity.ServiceOrderData;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendStatusUpdateNotification(ServiceOrderData os) {
        if (os.getClient() == null || os.getClient().getEmail() == null) {
            System.out.println("Nao foi possivel enviar email: Cliente sem email cadastrado.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("oficina@fiap.com.br");
            message.setTo(os.getClient().getEmail());
            message.setSubject("Atualizacao na OS #" + os.getId());
            
            StringBuilder text = new StringBuilder();
            text.append("Ola, ").append(os.getClient().getName()).append(".\n\n");
            text.append("O status da sua Ordem de Servico mudou para: ").append(os.getStatus()).append(".\n");
            
            if (os.getTotalAmount() != null) {
                text.append("Valor total atualizado: R$ ").append(os.getTotalAmount()).append("\n");
            }
            
            text.append("\nAtenciosamente,\nEquipe Oficina.");

            message.setText(text.toString());
            mailSender.send(message);
            
            System.out.println("Email enviado para " + os.getClient().getEmail());
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
        }
    }

    public void sendPaymentLink(ServiceOrderData os, String paymentUrl) {
        if (os.getClient() == null || os.getClient().getEmail() == null) {
            System.out.println("Nao foi possivel enviar email: Cliente sem email cadastrado.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("oficina@fiap.com.br");
            message.setTo(os.getClient().getEmail());
            message.setSubject("Aguardando Pagamento - OS #" + os.getId());
            
            StringBuilder text = new StringBuilder();
            text.append("Ola, ").append(os.getClient().getName()).append(".\n\n");
            text.append("O orcamento da sua Ordem de Servico foi finalizado.\n");
            text.append("Valor total: R$ ").append(os.getTotalAmount()).append("\n\n");
            text.append("Para aprovar e iniciar o servico, por favor realize o pagamento de forma segura atraves do link abaixo (Mercado Pago):\n\n");
            text.append(paymentUrl).append("\n\n");
            text.append("Atenciosamente,\nEquipe Oficina.");

            message.setText(text.toString());
            mailSender.send(message);
            
            System.out.println("📧 Email com link de cobranca enviado para " + os.getClient().getEmail());
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de cobranca: " + e.getMessage());
        }
    }
}