package evergoodteam.chassis.config.screen;

import evergoodteam.chassis.client.gui.text.ChassisScreenTexts;
import evergoodteam.chassis.client.gui.text.GradientText;
import evergoodteam.chassis.client.gui.widget.ResettableListWidget;
import evergoodteam.chassis.config.ConfigBase;
import evergoodteam.chassis.config.option.AbstractOption;
import evergoodteam.chassis.config.option.CategoryOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static evergoodteam.chassis.util.Reference.CMI;
import static org.slf4j.LoggerFactory.getLogger;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends ConfigOptionsScreen {

    private static final Logger LOGGER = getLogger(CMI + "/C/Screen");
    private ResettableListWidget list;
    private List<AbstractOption<?>> optionList;
    private Boolean retainValues;

    public ConfigScreen(Screen parent, ConfigBase config) {
        super(parent, config);
        this.retainValues = false;
    }

    @Override
    protected void init() {
        if (!retainValues) { // Prevent values from being reset
            optionList = new ArrayList<>();
            config.getHandler().readOptions();

            for (CategoryOption category : config.getOptionStorage().getCategories()) {
                if (!category.equals(config.getOptionStorage().getGenericCategory())) optionList.add(category);
                optionList.addAll(category.getOptions());
            }
        } else retainValues = false;

        this.refreshList();
        this.initFooter();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.list.render(context, mouseX, mouseY, delta);

        if (this.title instanceof GradientText) ((GradientText) this.title).scroll();
        this.drawCenteredGradientTitle(context);

        super.render(context, mouseX, mouseY, delta);

        List<OrderedText> tooltipList = getHoveredButtonTooltip(this.list, mouseX, mouseY);
        context.drawOrderedTooltip(textRenderer, tooltipList, mouseX, mouseY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (config.getHandler().writtenNotUpdated())
                this.client.setScreen(new ConfirmScreen(this::discardCallback, ChassisScreenTexts.DISCARD, ChassisScreenTexts.DISCARD_D));
            else client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void discardCallback(boolean discard) {
        if (discard) {
            this.close();
        } else {
            this.retainValues = true;
            this.client.setScreen(this);
        }
    }

    public void initFooter() {
        // Reset all
        this.addDrawableChild(ButtonWidget.builder(ChassisScreenTexts.RESET_A, (buttonWidget) -> {
            config.getOptionStorage().getOptions().forEach(AbstractOption::reset);

            this.refreshList();

            LOGGER.debug("Reset config options for \"{}\"", config.modid);
        }).position(this.width / 2 + 60, this.height - 27).size(100, 20).build());

        // Open .properties file
        this.addDrawableChild(ButtonWidget.builder(ChassisScreenTexts.OPEN, (buttonWidget) -> {
            config.openConfigFile();
        }).position(this.width / 2 - 160, this.height - 27).size(100, 20).build());

        // Save and close
        this.addDrawableChild(ButtonWidget.builder(ChassisScreenTexts.SAVE, (buttonWidget) -> {
            config.getOptionStorage().getOptions().forEach(option -> config.setWrittenValue(option.getName(), option.getValue()));
            config.getHandler().readOptions();

            LOGGER.debug("Saved config options for \"{}\"", config.modid);

            client.setScreen(this.parent);
        }).position(this.width / 2 - 50, this.height - 27).size(100, 20).build());
    }

    public void refreshList() {
        this.remove(this.list);
        this.list = new ResettableListWidget(this.client, this.width, this.height, 32, this.height - 32, 44);
        this.list.addAll(optionList.toArray(new AbstractOption[0]));
        this.addSelectableChild(this.list);
    }
}
