package test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;
public class TestThymeleaf {
    public static void main(String[] args) {
        try {
            FileTemplateResolver resolver = new FileTemplateResolver();
            resolver.setTemplateMode("HTML");
            resolver.setPrefix("c:/Users/HP/OneDrive/Desktop/R3Play/src/main/resources/templates/");
            resolver.setSuffix(".html");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);
            engine.process("profilo-utente", new Context());
            System.out.println("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
