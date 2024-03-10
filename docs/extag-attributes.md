## ExTag Attributes

### x-if

This tag is used with any __x-__ tag to hint that it should optionally be rendered, based on the evaluation of its expression value, which should 
result in either true or false.

```xml
<x-p id="done" x-text="name">name</x-p>
```
The parser will evaluate the _done_ expression, and if the result is _true_, the component will be rendered in the DOM, otherwise it will be skipped.

### x-items

This tag is used with any __x-__ tag to hint that it will be used in a _for-loop_ to generate markup for multiple related elements. It can optionally
be accompanied by the __x-key__ attribute which points to the unique field which identifies each object in the iterator.

```xml
<x-ul x-items="items" x-key="id">
  <li>
    <x-i x-if="done" class="fa fa-square-check" title="done"></x-i>
    <x-span x-text="title">title</x-span>
    <i class="fa fa-times-circle" title="remove"></i>
  </li>
</x-ul>
```

### x-key

This is only valid in the same context as __x-items__. It's optional, and its purpose is to inform about the field which uniquely identified each of
the objects in the iterator.

```xml
<x-ul x-items="items" x-key="id">...</x-ul>
```

### x-text

This tag should only be used with elements that are intended to display text content only. This tag, however, constrains the text content to the result 
of evaluating its expression value.

```xml
<x-span x-text="page.title">title</x-span>
```

This will result is evaluating the expression _page.title_ from the parser's context. If any text content exists, it will be overwritten by the evaluation
result.

### x-eval

Very closely related to the __x-text__ tag, this tag is also used with elements that are intended to display text content only. This tag, however, replaces 
the existing text content with the result of evaluating it first. In other words, it treats the existing _text content_ as an expression, whse result it will
use to replace the _expression_.

```xml
<x-p class="fa fa-memo" x-eval="true">My name is @{name}</x-p>
```

### x-path

This is only useful in the context of the __x-include__ tag. It provides the path (constrained by Java's classpath) to the target file whose content should
replace the __x-include__ tag

```xml
<p class="me">
  <x-include x-path="../another.html" />
</p>
```

### x-slot

This is used to designate positional elements in a layout page, where actual content will be swapped during rendering. These are important in retaining the 
ordering of elements in the final generated markup. 

> If the _target page_ does not have markup for the slot, then the existing content in the slot will be used in the resulting markup.

```xml
<html lang="en">
    <head>
    <meta charset="UTF-8" />
    <x-title x-slot="title">Default Title</x-title>
    </head>
    <body>
        <x-main x-slot="content"></x-main>
        <x-footer x-slot="footer">
            <div><span class="sticky">&amp;copy; 2024 ExTag</span></div>
        </x-footer>
    </body>
</html>
```

### x-named

This is used to indicate that the given __x-__ tag can be used as a document fragment for __ANY__ named _slot_ in a _layout template document_. 
Using the _layout template_ above (assuming it's called _basic-template.xml_), the following shows how it can be used

```xml
<x-layout x-template="/basic-template.xml">
    <x-title x-named="title" x-text="page.title">The good title</x-title>

    <x-div id="todo-list" x-named="content">Main content</x-div>
</x-layout>
```

### x-template

This is only useful in the context of the __x-layout__ tag. It provides the path (constrained by Java's classpath) to the target file whose content should
be used to decorate the _targeted page_.

### x-dcotype

Since the _<!DOCTYPE html>_ tag is not a valid _xml_ tag, the choice was made to use this tag purely for specifying the _doctype_ tag content, which is then
inserted into the generated markup when the markup generated begins.

```xml
<x-layout x-template="/basic-template.xml">
    
    <x-doctype x-doctype="&lt;!doctype html&gt;" />
    <!--...rest of it...-->
</x-layout>
```