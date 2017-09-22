package assignment.core.modal;

import assignment.model.Service;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ModalDispatcher {

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private interface ControllerCreator {
        ModalController create(Stage modalStage);
    };

    private Stage primaryStage;
    private IntegerProperty openModalsCount = new SimpleIntegerProperty(0);
    public BooleanProperty isModalOpen = new SimpleBooleanProperty(false);

    public ModalDispatcher(Stage primaryStage) {
        this.primaryStage = primaryStage;
        isModalOpen.bind(openModalsCount.isNotEqualTo(0));
    }

    private Object showModal(Stage stage, ControllerCreator controllerCreator) {
        Stage parentStage = (stage != null)
                ? stage
                : primaryStage;

        try {
            // Create the modal Stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.initOwner(parentStage);

            ModalController controller = controllerCreator.create(modalStage);
            modalStage.setTitle(controller.getTitle());

            FXMLLoader loader = new FXMLLoader(classLoader.getResource(controller.getTemplatePath()));
            loader.setController(controller);

            Parent layout = loader.load();
            modalStage.setScene(new Scene(layout));

            // Show the modal, wait until the user closes it
            // and then return the result
            openModalsCount.setValue(openModalsCount.getValue() + 1);
            modalStage.showAndWait();
            openModalsCount.setValue(openModalsCount.getValue() - 1);

            return controller.result();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Service showCreateServiceModal(Stage stage) {
        return (Service) showModal(stage, (Stage modalStage) -> {
            return new ServiceFormController(this, modalStage,
                    true, new Service());
        });
    }

    public Service showEditServiceModal(Stage stage, Service service) {
        return (Service) showModal(stage, (Stage modalStage) -> {
            return new ServiceFormController(this, modalStage,
                    false, new Service(service.name.get(), service.minAge.get(), service.description.get()));
        });
    }
}
