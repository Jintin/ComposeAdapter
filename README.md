# ComposeAdapter

ComposeAdapter is an Android tool to compose your Adapter with various ViewHolder by simple Annotations.

## Install

```groovy
implementation 'com.github.jintin:composeadapter:0.1.0'
kapt 'com.github.jintin:composeadapter-compiler:0.1.0'

```

## Usage

1. Add `@ViewHolder` annotation to your Adapter with layout and model information.
    - You can also mark `@HolderLayout` to your ViewHolder with layout id so adapter side can omit.
2. Change your super class to auto-generated class, name will as same as your current class name plus "Helper".
```kotlin
@ViewHolder(model = ViewHolder1::class)
@ViewHolder(layout = R.layout.item_holder2, model = ViewHolder2::class)
class SampleAdapter(private val list: List<String>) : SampleAdapterHelper() {
    //...
}
@HolderLayout(R.layout.item_holder1)
class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //...
}
class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //...
}
```
3. Build project, the related `onCreateViewHolder` method will be created in the auto-generated super class with static viewType int for further usage.
```java
public abstract class SampleAdapterHelper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  protected static final int TYPE_VIEW_HOLDER1 = 0;

  protected static final int TYPE_VIEW_HOLDER2 = 1;

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_VIEW_HOLDER1: {
        View view = LayoutInflater.from(parent.getContext()).inflate(2131296285, parent, false);
        return new ViewHolder1(view);
      }
      case TYPE_VIEW_HOLDER2: {
        View view = LayoutInflater.from(parent.getContext()).inflate(2131296286, parent, false);
        return new ViewHolder2(view);
      }
      default: throw new RuntimeException("Not support type" + viewType);
    }
  }
}

```

## Contributing

Bug reports and pull requests are welcome on GitHub at <https://github.com/Jintin/ComposeAdapter>.

## License

The module is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
