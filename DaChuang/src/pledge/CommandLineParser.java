package pledge;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 *
 * @author Christopher Henard
 */
public class CommandLineParser {

    private JCommander jCommander;
    private Generate commandGenerate;
    private Prioritize commandPrioritize;
    private String[] args;
    public static final String GENERATE = "generate_products";
    public static final String PRIORITIZE = "prioritize_products";

    public CommandLineParser(String[] args, String programName) {
        this.args = args;

        commandGenerate = new Generate();
        commandPrioritize = new Prioritize();
        //它是一个命令行参数解析库，用于处理命令行参数的解析和管理
        jCommander = new JCommander();
        //分别将两个命令和它们对应的参数配置对象注册到 JCommander 中
        jCommander.addCommand(GENERATE, commandGenerate);
        jCommander.addCommand(PRIORITIZE, commandPrioritize);
        jCommander.setProgramName("java -jar " + programName + ".jar");

    }

    //@Parameters 是JCommander 库提供的注解，它用于标识这个内部类是一个命令的参数配置
    //@Parameter 注解用于描述具体的命令行参数选项。
    @Parameters(commandDescription = "Generate products")
    public class Generate {

        //特征模型文件的路径
        @Parameter(names = "-fm", description = "Feature model (SPLOT format by default)", required = true)
        public String fmFile;
        //生成产品输出文件的路径
        @Parameter(names = "-o", description = "Output file", required = true)
        public String outputFile;
        //要生成产品的数量
        @Parameter(names = "-nbProds", description = "Number of products to generate.")
        public int nbProds = 10;
        //时间限制
        @Parameter(names = "-timeAllowedMS", description = "Time allowed for the generation in ms")
        public long timeAllowed = 60000;
        //指定特征模型是否是dimac格式文件
        @Parameter(names = "-dimacs", description = "Specify if the FM is a dimacs one")
        public boolean dimacs = false;
    }
    
    @Parameters(commandDescription = "Prioritize products")
    public class Prioritize {

        //输入产品文件的路径
        @Parameter(names = "-i", description = "Input products file", required = true)
        public String inputFile;
        //指定优化技术的选择greedy or nearoptimal
        @Parameter(names = "-t", description = "technique (greedy or nearoptimal)", required = true)
        public String technique;
        //输出文件的路径
        @Parameter(names = "-o", description = "Output file", required = true)
        public String outputFile;

    }

    public Generate getCommandGenerate() {
        return commandGenerate;
    }

    //程序会根据预定义的参数配置（在 CommandLineParser 类中定义的 Generate 和 Prioritize 内部类中的注解）
    // 来解析 args 中的命令行参数，并将它们映射到相应的参数配置对象中
    public void parseArgs() {
        jCommander.parse(args);
    }

    public String getCommandName() {
        return jCommander.getParsedCommand();
    }

    public void printUsage() {
        jCommander.usage();
    }
}
