package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SuperRecyclerView extends RecyclerView {

    private SuperAdapter superAdapter;

    public SuperRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public SuperRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuperRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext()));
        superAdapter = new SuperAdapter();
        setAdapter(superAdapter);
    }

    /**
     * 设置数据项并指定视图创建和数据绑定逻辑
     */
    public <T> void setItems(List<T> items,
                             ViewCreator viewCreator,
                             Binder<T> binder) {
        superAdapter.setItems(items, viewCreator, binder);
    }

    /**
     * 设置多种视图类型的数据
     */
    public <T> void setItemsWithViewTypes(List<T> items,
                                          ViewTypeResolver<T> viewTypeResolver,
                                          ViewCreator viewCreator,
                                          Binder<T> binder) {
        superAdapter.setItemsWithViewTypes(items, viewTypeResolver, viewCreator, binder);
    }

    /**
     * 设置点击监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        superAdapter.setOnItemClickListener(listener);
    }

    /**
     * 设置长按监听器
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        superAdapter.setOnItemLongClickListener(listener);
    }

    /**
     * 视图创建接口
     */
    public interface ViewCreator {
        View createView(ViewGroup parent, int viewType);
    }

    /**
     * 数据绑定接口
     */
    public interface Binder<T> {
        void bind(View view, T item, int position);
    }

    /**
     * 视图类型解析接口
     */
    public interface ViewTypeResolver<T> {
        int getViewType(T item);
    }

    /**
     * 点击监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 长按监听接口
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    /**
     * 内部适配器实现
     */
    private static class SuperAdapter extends Adapter<SuperViewHolder> {
        private List<Object> items = new ArrayList<>();
        private ViewCreator viewCreator;
        private Binder<Object> binder;
        private ViewTypeResolver<Object> viewTypeResolver;
        private OnItemClickListener itemClickListener;
        private OnItemLongClickListener itemLongClickListener;

        public <T> void setItems(List<T> items, ViewCreator viewCreator, Binder<T> binder) {
            this.items = new ArrayList<>(items);
            this.viewCreator = viewCreator;
            this.binder = (view, item, position) -> binder.bind(view, (T) item, position);
            this.viewTypeResolver = null;
            notifyDataSetChanged();
        }

        public <T> void setItemsWithViewTypes(List<T> items,
                                              ViewTypeResolver<T> viewTypeResolver,
                                              ViewCreator viewCreator,
                                              Binder<T> binder) {
            this.items = new ArrayList<>(items);
            this.viewTypeResolver = item -> viewTypeResolver.getViewType((T) item);
            this.viewCreator = viewCreator;
            this.binder = (view, item, position) -> binder.bind(view, (T) item, position);
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.itemClickListener = listener;
        }

        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.itemLongClickListener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            if (viewTypeResolver != null) {
                return viewTypeResolver.getViewType(items.get(position));
            }
            return 0;
        }

        @NonNull
        @Override
        public SuperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewCreator == null) {
                throw new IllegalStateException("View creator must be set");
            }
            View view = viewCreator.createView(parent, viewType);
            // 确保item视图宽度铺满
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            } else {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            view.setLayoutParams(params);
            SuperViewHolder holder = new SuperViewHolder(view);
            // 设置点击监听
            view.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, holder.getAdapterPosition());
                }
            });
            view.setOnLongClickListener(v -> {
                if (itemLongClickListener != null) {
                    return itemLongClickListener.onItemLongClick(v, holder.getAdapterPosition());
                }
                return false;
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull SuperViewHolder holder, int position) {
            if (binder != null) {
                binder.bind(holder.itemView, items.get(position), position);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * ViewHolder 实现
     */
    private static class SuperViewHolder extends ViewHolder {
        public SuperViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}