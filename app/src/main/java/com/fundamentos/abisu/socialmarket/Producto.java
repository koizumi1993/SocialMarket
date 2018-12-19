package com.fundamentos.abisu.socialmarket;

public class Producto {
    String producto;
    String precio;
    String descripcion;
    String foto;
    String vendedor;

    public Producto() {
    }

    public Producto(String producto, String precio, String descripcion, String foto, String vendedor) {
        this.producto = producto;
        this.precio = precio;
        this.descripcion = descripcion;
        this.foto = foto;
        this.vendedor = vendedor;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }
}
