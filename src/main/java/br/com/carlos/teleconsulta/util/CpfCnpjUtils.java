package br.com.carlos.teleconsulta.util;

public final class CpfCnpjUtils {

    private CpfCnpjUtils() {}

    public static String digitsOnly(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    public static boolean isValidCPF(String cpf) {
        cpf = digitsOnly(cpf);
        if (cpf == null || cpf.length() != 11) return false;

        if (cpf.chars().distinct().count() == 1) return false;

        int dig1 = calcCpfDigit(cpf.substring(0, 9));
        int dig2 = calcCpfDigit(cpf.substring(0, 9) + dig1);
        return cpf.equals(cpf.substring(0, 9) + dig1 + dig2);
    }

    private static int calcCpfDigit(String base) {
        int soma = 0;
        int pesoInicial = base.length() + 1;
        for (int i = 0; i < base.length(); i++) {
            int num = base.charAt(i) - '0';
            soma += num * (pesoInicial - i);
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    public static boolean isValidCNPJ(String cnpj) {
        cnpj = digitsOnly(cnpj);
        if (cnpj == null || cnpj.length() != 14) return false;

        if (cnpj.chars().distinct().count() == 1) return false;

        int dig1 = calcCnpjDigit(cnpj.substring(0, 12));
        int dig2 = calcCnpjDigit(cnpj.substring(0, 12) + dig1);
        return cnpj.equals(cnpj.substring(0, 12) + dig1 + dig2);
    }

    private static int calcCnpjDigit(String base) {
        int[] pesos = (base.length() == 12)
                ? new int[]{5,4,3,2,9,8,7,6,5,4,3,2}
                : new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2};

        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            int num = base.charAt(i) - '0';
            soma += num * pesos[i + (pesos.length - base.length())];
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}
