package rubbles.monitoring.commcoverage;

import com.glowbyte.a366.crypt.blowfish.Encryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import rubbles.monitoring.commcoverage.adapter.MonitoringCommCoverageAdapter;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

	@Autowired
	private MonitoringCommCoverageAdapter monitoringNpsAdapter;

	public static void main(String[] args) {
		parseArgs(args);
	}

	private static void parseArgs(String[] args) {
		int checkFlag = 0;
		String action = "";
		String value2crypt = "";

		if (args.length == 0) {
			System.out.println("Запуск скрипта требует обязательного указания аргументов:");
			System.out.println(" -action crypt");
			System.out.println("	Шифрование пароля");
			System.out.println("	-value2crypt <значение>");
			System.out.println("		Значение для шифрования");
			System.out.println(" -action run");
			System.out.println("	Запуск основной логики скрипта");
			checkFlag = 1;
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				if ((i + 1) == args.length) {
					log.error("Value of parameter " + args[i] + " not defined");
					System.out.printf("ОШИБКА: Значение аргумента %s не определено%n",args[i]);
					checkFlag = 1;
				} else {
					if ((i + 1) < args.length && !args[i + 1].startsWith("-")) {
						if (args[i].equalsIgnoreCase("-action")) {
							action = args[i + 1];
						} else if (args[i].equalsIgnoreCase("-value2crypt")) {
							value2crypt = args[i + 1];
						} else {
							log.error("Parameter not determined : " + args[i]);
							log.info("Доступные параметры : -action -value2crypt");
							checkFlag = 1;
						}
					} else if ((i + 1) < args.length && args[i + 1].startsWith("-")) {
						log.error("Value of parameter " + args[i] + " not defined");
						checkFlag = 1;
					}
				}
			} else if (i > 0 && !args[i - 1].startsWith("-")) {
				log.error("Parameter: " + args[i] + " not found");
				System.out.printf("ОШИБКА: Аргумент %s не найден%n",args[i]);
				checkFlag = 1;
			}
		}

		if (checkFlag == 0) {
			if (action.equalsIgnoreCase("crypt")) {
				if (!value2crypt.isEmpty()) {
					try {
						System.out.println("Зашифрованное значение: " + Encryptor.encrypt(value2crypt));
					} catch (Exception e) {
						System.out.println("Не удалось зашифровать сообщение: " + e.getMessage());
					}
				}
				else {
					System.out.println("ОШИБКА: В случае использования значения \"crypt\" для аргумента -action обязательно должен быть указан аргумент -value2crypt");
				}
			}
			else if (action.equalsIgnoreCase("run")) {
				SpringApplication.run(Application.class, args);
			}
		}
	}

	@Override
	public void run(String[] args) throws Exception {
		monitoringNpsAdapter.run();
	}
}
