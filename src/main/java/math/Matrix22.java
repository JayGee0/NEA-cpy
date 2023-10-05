package math;

import java.util.Arrays;

public class Matrix22 {

    private float[][] row_column;

    /**
     * | r0c0   r0c1 |
     * |             |
     * | r1c0   r1c1 |
     */


    public Matrix22() {
        this.row_column = new float[2][2];
        this.row_column[0][0] = 1.0f;
        this.row_column[0][1] = 0.0f;
        this.row_column[1][0] = 0.0f;
        this.row_column[1][1] = 1.0f;
    }

    public Matrix22(Matrix22 mat) {
        this.row_column = new float[2][2];
        setMatrix(mat);
    }

    public Matrix22(float[][] row_column) {
        this.row_column = row_column.clone();
    }


    public Matrix22(float m00, float m01,
                    float m10, float m11) {
        this.row_column = new float[2][2];
        this.row_column[0][0] = m00;
        this.row_column[0][1] = m01;
        this.row_column[1][0] = m10;
        this.row_column[1][1] = m11;
    }

    public float getRowColumn(int row, int col) {
        return row_column[row][col];
    }

    public Matrix22 setRowColumn(int row, int col, float value) {
        row_column[row][col] = value;
        return this;
    }

    public Matrix22 set(float m00, float m01, float m10, float m11) {
        this.row_column[0][0] = m00;
        this.row_column[0][1] = m01;
        this.row_column[1][0] = m10;
        this.row_column[1][1] = m11;
        return this;
    }

    private void setMatrix(Matrix22 mat) {
        row_column = mat.row_column.clone();
    }

    public Matrix22 mul(Matrix22 right) {
        Matrix22 result = new Matrix22();
        result.row_column[0][0] = row_column[0][0] * right.row_column[0][0] + row_column[0][1] * right.row_column[1][0];
        result.row_column[0][1] = row_column[0][0] * right.row_column[0][1] + row_column[0][1] * right.row_column[1][1];
        result.row_column[1][0] = row_column[1][0] * right.row_column[0][0] + row_column[1][1] * right.row_column[1][0];
        result.row_column[1][1] = row_column[1][0] * right.row_column[0][1] + row_column[1][1] * right.row_column[1][1];
        this.setMatrix(result);
        return this;
    }

    public Vector2 mul(Vector2 right) {
        float x = row_column[0][0] * right.getX() + row_column[0][1] * right.getY();
        float y = row_column[1][0] * right.getX() + row_column[1][1] * right.getY();
        return new Vector2(x,y);
    }

    public float determinant() {
        // ad - bc
        return row_column[0][0] * row_column[1][1] - row_column[0][1] * row_column[1][0];
    }

    public Matrix22 inverse() {
        float s = 1.0f / determinant();
        float newM00 = row_column[1][1] * s;
        float newM01 = -row_column[0][1] * s;
        float newM10 = -row_column[1][0] * s;
        float newM11 = row_column[0][0] * s;
        set(newM00, newM01, newM10, newM11);
        return this;
    }

    public Matrix22 transpose() {
        Matrix22 result = new Matrix22();
        result.row_column[0][0] = row_column[0][0];
        result.row_column[0][1] = row_column[1][0];
        result.row_column[1][0] = row_column[0][1];
        result.row_column[1][1] = row_column[1][1];
        setMatrix(result);
        return this;
    }

    public Matrix22 absolute() {
        this.row_column[0][0] = Math.abs(row_column[0][0]);
        this.row_column[0][1] = Math.abs(row_column[0][1]);
        this.row_column[1][0] = Math.abs(row_column[1][0]);
        this.row_column[1][1] = Math.abs(row_column[1][1]);
        return this;
    }

    public String toString() {
        return "[" + row_column[0][0] + " " + row_column[0][1] + "]\n"
             + "[" + row_column[1][0] + " " + row_column[1][1] + "]";
    }


    public Matrix22 identity() {
        this.row_column[0][0] = 1.0f;
        this.row_column[0][1] = 0.0f;
        this.row_column[1][0] = 0.0f;
        this.row_column[1][1] = 1.0f;
        return this;
    }

    public Matrix22 rotation(float angle) {
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        row_column[0][0] = cos;
        row_column[0][1] = -sin;
        row_column[1][0] = sin;
        row_column[1][1] = cos;
        return this;
    }

    public Vector2 getColumn(int col) {
        Vector2 column = new Vector2();
        switch (col) {
            case 0:
                column.set(row_column[0][0], row_column[1][0]);
                break;
            case 1:
                column.set(row_column[0][1], row_column[1][1]);
                break;
            default:
                throw new IllegalStateException("Column out of bounds: " + col);
        }
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matrix22 matrix22 = (Matrix22) o;
        return Arrays.equals(row_column, matrix22.row_column);
    }

}
