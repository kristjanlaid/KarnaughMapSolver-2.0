package project.karnaughmapsolver;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import java.util.*;
import java.util.stream.Collectors;

public class CubeModelBuilder {
    private Group sceneRoot;
    private PerspectiveCamera perspectiveCamera;
    private int numberOfVariables;
    private KMap kMap;
    private Group meshGroup;

    public static final float X_RED     = 0.5f / 7f;
    public static final float X_GREEN   = 1.5f / 7f;
    public static final float X_BLUE    = 2.5f / 7f;
    public static final float X_YELLOW  = 3.5f / 7f;
    public static final float X_ORANGE  = 4.5f / 7f;
    public static final float X_WHITE   = 5.5f / 7f;
    public static final float X_GRAY    = 6.5f / 7f;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;


    public CubeModelBuilder(Group sceneRoot, PerspectiveCamera perspectiveCamera, int numberOfVariables, KMap kMap) {
        this.sceneRoot = sceneRoot;
        this.perspectiveCamera = perspectiveCamera;
        this.numberOfVariables = numberOfVariables;
        this.kMap = kMap;
    }

    public List<int[]> createPatternFaceList(int numberOfVariables) {
        int[] dimensions = new int[3];
        List<int[]> patternFaces = new ArrayList<>();
        if (numberOfVariables == 5) {
            dimensions[0] = 4;
            dimensions[1] = 4;
            dimensions[2] = 2;
        }

        if (numberOfVariables == 6) {
            dimensions[0] = 4;
            dimensions[1] = 4;
            dimensions[2] = 4;
        }

        if (numberOfVariables == 7) {
            dimensions[0] = 8;
            dimensions[1] = 4;
            dimensions[2] = 4;
        }

        for (int i = 0; i < dimensions[2]; i++) {
            for (int j = 0; j < dimensions[1]; j++) {
                for (int k = 0; k < dimensions[0]; k++) {
                    int[] coordTempArray = new int[6];
                    coordTempArray[0] = k;
                    coordTempArray[1] = j;
                    coordTempArray[2] = i;

                    coordTempArray[3] = 0;
                    coordTempArray[4] = 0;
                    coordTempArray[5] = 0;

                    patternFaces.add(coordTempArray);
                }
            }
        }

        return patternFaces;
    }

    public HashMap<List<Integer>, Point3D> createPointList(int numberOfVariables) {
        int[] dimensions = new int[3];
        if (numberOfVariables == 5) {
            dimensions[0] = 4;
            dimensions[1] = 4;
            dimensions[2] = 2;
        }

        if (numberOfVariables == 6) {
            dimensions[0] = 4;
            dimensions[1] = 4;
            dimensions[2] = 4;
        }

        if (numberOfVariables == 7) {
            dimensions[0] = 8;
            dimensions[1] = 4;
            dimensions[2] = 4;
        }

        List<Point3D> points = new ArrayList<>();
        HashMap<List<Integer>, Point3D> coordAndPoints = new HashMap<>();
        double zCoord = -2.1;
        for (int i = 0; i < dimensions[2]; i++) {
            double yCoord = -2.1;
            for (int j = 0; j < dimensions[1]; j++) {
                double xCoord = -2.1;
                for (int k = 0; k < dimensions[0]; k++) {

                    List<Integer> coordArray = new ArrayList<>();
                    coordArray.add(k);
                    coordArray.add(j);
                    coordArray.add(i);

                    Point3D point = new Point3D(xCoord, yCoord, zCoord);
                    coordAndPoints.put(coordArray, point);
                    points.add(point);
                    xCoord += 2.1;
                }
                yCoord += 2.1;
            }
            zCoord += 2.1;
        }

        return coordAndPoints;

    }

    private TriangleMesh createCube(int[] face) {
        TriangleMesh m = new TriangleMesh();
        // POINTS
        m.getPoints().addAll(
                0.5f,  0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                -0.5f,  0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,
                -0.5f,  0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f
        );

        // TEXTURES
        m.getTexCoords().addAll(
                X_RED, 0.5f,
                X_GREEN, 0.5f,
                X_BLUE, 0.5f,
                X_YELLOW, 0.5f,
                X_ORANGE, 0.5f,
                X_WHITE, 0.5f,
                X_GRAY, 0.5f
        );


        // FACES
        m.getFaces().addAll(
                2,face[0],3,face[0],6,face[0],      // F
                3,face[0],7,face[0],6,face[0],

                0,face[1],1,face[1],2,face[1],      // R
                2,face[1],1,face[1],3,face[1],

                1,face[2],5,face[2],3,face[2],      // U
                5,face[2],7,face[2],3,face[2],

                0,face[3],4,face[3],1,face[3],      // B
                4,face[3],5,face[3],1,face[3],

                4,face[4],6,face[4],5,face[4],      // L
                6,face[4],7,face[4],5,face[4],

                0,face[5],2,face[5],4,face[5],      // D
                2,face[5],6,face[5],4,face[5]
        );
        return m;
    }

    public SubScene createScene() {
        SubScene subScene = new SubScene(sceneRoot, 350, 350, true, SceneAntialiasing.BALANCED);
        perspectiveCamera.setNearClip(0.5);
        perspectiveCamera.setFarClip(10000.0);
        perspectiveCamera.setTranslateZ(-25);

        subScene.setCamera(perspectiveCamera);

        meshGroup = new Group();
        HashMap<List<Integer>, Point3D> facePoints = createPointList(numberOfVariables);
        List<int[]> patternFaces = createPatternFaceList(numberOfVariables);

//        patternFaceF.forEach(p -> {
//            MeshView meshP = new MeshView();
//            meshP.setMesh(createCube(p));
//            meshP.setMaterial(mat);
//            Point3D pt = pointsFaceF.get(cont.getAndIncrement());
//            meshP.getTransforms().addAll(new Translate(pt.getX(), pt.getY(), pt.getZ()));
//            meshGroup.getChildren().add(meshP);
//        });

        for (int[] p : patternFaces) {
            MeshView meshP = new MeshView(createCube(p));
            PhongMaterial mat = new PhongMaterial();
            ValueSet value = kMap.getValue(p[0], p[1], p[2]);
            Character one = '1';
            Character question = '?';
            if (one.equals(value.getF())) {
                mat.setDiffuseColor(Color.BLACK);
            } else if (question.equals(value.getF())) {
                mat.setDiffuseColor(Color.GRAY);
            } else {
                mat.setDiffuseColor(Color.WHITE);
            }
            meshP.setMaterial(mat);

            List<Integer> tempList = Arrays.asList(p[0], p[1], p[2]);

            Point3D pt = facePoints.get(tempList);
            meshP.getTransforms().addAll(new Translate(pt.getX(), pt.getY(), pt.getZ()));
            meshGroup.getChildren().add(meshP);
        }

        Rotate rotateX = new Rotate(30, 0, 0, 0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(20, 0, 0, 0, Rotate.Y_AXIS);
        meshGroup.getTransforms().addAll(rotateX, rotateY);

        sceneRoot.getChildren().addAll(meshGroup, new AmbientLight(Color.WHITE));

        subScene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        subScene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle()-(mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle()+(mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        return subScene;
    }

    public void refreshCubeValues() {
        HashMap<List<Integer>, Point3D> facePoints = createPointList(numberOfVariables);

        for (int i = 0; i < meshGroup.getChildren().size(); i++) {
            MeshView meshView = (MeshView) meshGroup.getChildren().get(i);
            Transform meshTransform = meshView.getTransforms().get(0);
            Point3D point3D = new Point3D(meshTransform.getTx(), meshTransform.getTy(), meshTransform.getTz());
            List<List<Integer>> resultList = facePoints.entrySet().stream()
                    .filter(entry -> Objects.equals(entry.getValue(), point3D))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            List<Integer> keyMapCoords = resultList.get(0);
            ValueSet keyMapValues = kMap.getValue(keyMapCoords.get(0), keyMapCoords.get(1), keyMapCoords.get(2));
            PhongMaterial mat = new PhongMaterial();
            Character one = '1';
            Character question = '?';
            if (one.equals(keyMapValues.getF())) {
                mat.setDiffuseColor(Color.BLACK);
            } else if (question.equals(keyMapValues.getF())) {
                mat.setDiffuseColor(Color.GRAY);
            } else {
                mat.setDiffuseColor(Color.WHITE);
            }

            if (keyMapValues.isClicked()) {
                mat.setDiffuseColor(Color.BLUE);
            }
            meshView.setMaterial(mat);
        }
    }

}
