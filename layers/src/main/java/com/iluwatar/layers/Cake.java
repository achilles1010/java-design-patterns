/**
 * The MIT License
 * Copyright (c) 2014-2016 Ilkka Seppälä
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.layers;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Cake entity
 *
 */
@Entity
public class Cake {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.REMOVE)
    private CakeTopping topping;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private Set<CakeLayer> layers;

    public Cake() {
        setLayers(new HashSet<>());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CakeTopping getTopping() {
        return topping;
    }

    public void setTopping(CakeTopping topping) {
        this.topping = topping;
    }

    public Set<CakeLayer> getLayers() {
        return layers;
    }

    public final void setLayers(Set<CakeLayer> layers) {
        this.layers = layers;
    }

    public void addLayer(CakeLayer layer) {
        this.layers.add(layer);
    }

    @Override
    public String toString() {
        return String.format("id=%s topping=%s layers=%s", id, topping, layers.toString());
    }
}
