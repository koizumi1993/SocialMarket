package com.fundamentos.abisu.socialmarket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.HolderProducto> {
    ArrayList<Producto> productos;
    Context context;

    public ProductoAdapter(ArrayList<Producto> productos) {
        this.productos = productos;
    }

    public void addProducto(Producto producto){
        productos.add(producto);
        notifyItemInserted(productos.size());
    }

    @NonNull
    @Override
    public HolderProducto onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_producto,viewGroup,false);
        return new HolderProducto(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProducto holderProducto, int i) {
        holderProducto.producto.setText(productos.get(i).getProducto());
        holderProducto.precio.setText(productos.get(i).getPrecio());
        holderProducto.descripcion.setText(productos.get(i).getDescripcion());
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public class HolderProducto extends RecyclerView.ViewHolder{
        TextView producto, precio, descripcion;

        public HolderProducto(@NonNull View itemView) {
            super(itemView);
            producto = (TextView) itemView.findViewById(R.id.txtNombreProducto);
            precio = (TextView) itemView.findViewById(R.id.txtPrecioProducto);
            descripcion = (TextView) itemView.findViewById(R.id.txtDescriProd);
        }
    }
}
