/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.console.wicket.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class AjaxPasswordFieldPanel extends FieldPanel<String> {

    private static final long serialVersionUID = -5490115280336667460L;

    public AjaxPasswordFieldPanel(
            final String id,
            final String name,
            final IModel<String> model,
            final boolean active) {

        super(id, name, model, active);

        field = new PasswordTextField(
                "passwordField", model).setResetPassword(false);

        add(field.setLabel(new Model(name)).setOutputMarkupId(true));

        if (active) {
            field.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(final AjaxRequestTarget art) {
                    // nothing to do
                }
            });
        }
    }

    @Override
    public FieldPanel addRequiredLabel() {

        if (!isRequired()) {
            setRequired(true);
        }

        this.isRequiredLabelAdded = true;
        return this;
    }
}
