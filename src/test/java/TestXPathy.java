import com.xpathy.sourceCode.And;

import static com.xpathy.sourceCode.Attribute.*;

import static com.xpathy.sourceCode.Case.*;

import static com.xpathy.sourceCode.Condition.*;

import static com.xpathy.sourceCode.Only.*;

import com.xpathy.sourceCode.Or;

import static com.xpathy.sourceCode.Style.*;

import static com.xpathy.sourceCode.Tag.*;

import com.xpathy.sourceCode.Text;
import com.xpathy.sourceCode.XPathy;

//overlapping
import static com.xpathy.sourceCode.Attribute.title;

public class TestXPathy {


    XPathy pathy1 = new XPathy();

    XPathy locator2 = id.contains("login-button");
    // Result: //*[contains(@id, 'login-button')]

    XPathy locator3 = div.byAttribute(id).equals("main-container");
    // Result: //div[@id='main-container']

    XPathy locator4 = Text.contains("Error");
    // Result: //*[contains(text(), 'Error')]

    XPathy locator5 = Text.startsWith("Success");
    // Result: //*[starts-with(text(), 'Success')]

    XPathy locator = backgroundColor.equals("#000000");
    // Result: //*[contains(translate(@style, ' ', ''), 'background-color:#000000;')]

    XPathy getLocator2 = title.equals("a");

    XPathy locator23 = div.byAttribute(class_).equals("container")
            .$tag(button)
            .byText().equals("Submit");

    //Result:
    //div[@class='container']//button[text()='Submit']

    XPathy locator34 = button.byAttribute(id)
            .withCase(IGNORED)
            .contains("login-button");

    //Result:
    //button[contains(translate(@id, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'login-button')]


    XPathy locator43 = span.byText()
            .withKeepOnly(ENGLISH_ALPHABETS)
            .contains("ProductABC");

    //Result:
    //span[contains(translate(text(), translate(text(), 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', ''), ''), 'ProductABC')]


    XPathy locator65 = button.byAttribute(id)
            .union(Or.equals("login-btn"),
                    Or.equals("signin-btn"),
                    Or.contains("auth"));

    // Result:
    //button[@id='login-btn' or @id='signin-btn' or contains(@id, 'auth')]

    XPathy locator22 = div.byText()
            .intersect(And.startsWith("Order #"),
                    And.contains("Confirmed"),
                    And.not().contains("Cancelled"));

    // Result:
    //div[starts-with(text(), 'Order #') and contains(text(), 'Confirmed') and not(contains(text(), 'Cancelled'))]

    XPathy locator89 = div.byCondition(
            and(
                    text().startsWith("Login"),

                    or(
                            text().contains("Button"),
                            attribute(id).contains("auth-btn")
                    ),

                    not(attribute(class_).withCase(IGNORED).contains("disabled"))
            )
    );

    // Result:
    //div[(starts-with(text(), 'Login')
    //      and (contains(text(), 'Button') or contains(@id, 'auth-btn'))
    //      and not(contains(translate(@class, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'disabled')))]


    XPathy locator2w = span.byCondition(
            or(
                    text().contains("Premium"),

                    and(
                            attribute(class_).equals("highlight"),
                            attribute(data_testid).contains("featured"),
                            not(text().contains("Expired"))
                    )
            )
    );

    // Result:
    //span[
    //   contains(text(), 'Premium')
    //   or (
    //       @class='highlight'
    //       and contains(@data-testid, 'featured')
    //       and not(contains(text(), 'Expired'))
    //   )
    //]


    XPathy getLocator2s = div.byCondition(and(
                    attribute(class_).equals("ss"),
                    attribute(data_testid).contains("tt"),

                    or(
                            text().contains("XX"),
                            attribute(id).equals("yy")
                    ),
                    not(attribute(id).equals("yy"))
            )
    );

    XPathy locator3d = div.byAttribute(class_).equals("product-card").and()
            .byHaving(
                    span.byText().contains("In Stock")
            );

    // Result:
    //div[@class='product-card' and ( span[contains(text(), 'In Stock')] )]


}
