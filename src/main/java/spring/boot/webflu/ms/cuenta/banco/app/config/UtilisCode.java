package spring.boot.webflu.ms.cuenta.banco.app.config;

import java.util.Random;

public class UtilisCode {
	
	public static String numTarjeta() {
		Random rand = new Random();
        // Generar un número aleatorio de 4 dígitos
        int numeroAleatorio = rand.nextInt(9000) + 1000;
        String numero = String.valueOf(numeroAleatorio);        
        return numero;
	}
	
	public static String numCuenta() {
		Random rand = new Random();
        // Generar un número aleatorio de 4 dígitos
        int numeroAleatorio = rand.nextInt(9000) + 1000;
        String numero = String.valueOf(numeroAleatorio);        
        return numero;
	}

}
