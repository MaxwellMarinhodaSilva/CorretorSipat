package com.corretorsipat.parser;

import com.corretorsipat.model.RegistroSip;
import com.corretorsipat.service.SipatValidationException;

import java.math.BigInteger;

/** Extrai e atualiza somente os campos definidos no layout SIPAT. */
public final class SipatParser {
    private SipatParser() {
    }

    /** Valida uma linha de 800 caracteres e extrai os campos aplicáveis ao seu tipo. */
    public static RegistroSip parse(String linha, int numeroLinha) throws SipatValidationException {
        validarTamanho(linha, numeroLinha);
        char tipo = linha.charAt(LayoutSipat.TIPO_INICIO);
        String sequencial = extrair(linha, LayoutSipat.SEQUENCIAL_INICIO, LayoutSipat.SEQUENCIAL_FIM);
        if (!sequencial.matches("\\d{5}")) {
            throw erro(numeroLinha, "campo sequencial inválido: '" + sequencial + "'.");
        }

        if (tipo == '1') {
            String valorTexto = extrair(linha, LayoutSipat.VALOR_INICIO, LayoutSipat.VALOR_FIM);
            String protocolo = extrair(linha, LayoutSipat.PROTOCOLO_INICIO, LayoutSipat.PROTOCOLO_FIM);
            if (!valorTexto.matches("\\d{14}")) {
                throw erro(numeroLinha, "valor deve conter exatamente 14 dígitos.");
            }
            if (!protocolo.matches("\\d{14}")) {
                throw erro(numeroLinha, "protocolo deve conter exatamente 14 dígitos.");
            }
            return new RegistroSip(numeroLinha, tipo, linha, new BigInteger(valorTexto), protocolo, sequencial);
        }
        return new RegistroSip(numeroLinha, tipo, linha, null, null, sequencial);
    }

    /** Garante o tamanho fixo antes de qualquer acesso posicional por substring. */
    public static void validarTamanho(String linha, int numeroLinha) throws SipatValidationException {
        if (linha == null || linha.length() != LayoutSipat.TAMANHO_REGISTRO) {
            int tamanho = linha == null ? 0 : linha.length();
            throw erro(numeroLinha, "tamanho " + tamanho + "; esperado: 800 caracteres.");
        }
    }

    /** Obtém um campo usando índices já centralizados em {@link LayoutSipat}. */
    public static String extrair(String linha, int inicio, int fim) {
        return linha.substring(inicio, fim);
    }

    /** Substitui um campo sem alterar o comprimento total do registro posicional. */
    public static String substituir(String linha, int inicio, int fim, String valor) {
        if (linha == null || linha.length() != LayoutSipat.TAMANHO_REGISTRO) {
            throw new IllegalArgumentException("A linha deve possuir exatamente 800 caracteres.");
        }
        if (valor == null || valor.length() != fim - inicio) {
            throw new IllegalArgumentException("O valor substituto deve possuir " + (fim - inicio) + " caracteres.");
        }
        String resultado = linha.substring(0, inicio) + valor + linha.substring(fim);
        if (resultado.length() != LayoutSipat.TAMANHO_REGISTRO) {
            throw new IllegalStateException("A substituição alterou o tamanho do registro.");
        }
        return resultado;
    }

    /** Compara detalhes desconsiderando apenas o sequencial físico do fim da linha. */
    public static boolean conteudoIgualExcetoSequencial(String primeira, String outra) {
        return primeira.substring(0, LayoutSipat.SEQUENCIAL_INICIO)
                .equals(outra.substring(0, LayoutSipat.SEQUENCIAL_INICIO));
    }

    private static SipatValidationException erro(int linha, String detalhe) {
        return new SipatValidationException("Linha " + linha + ": " + detalhe);
    }
}
