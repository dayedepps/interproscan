/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 * BlastProDom raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlTransient
public class RawBlastProDomMatch extends RawMatch<BlastProDomLocation> {

    protected RawBlastProDomMatch() {}

    public RawBlastProDomMatch(Model model) {
        super(model);
    }

    public RawBlastProDomMatch(Model model, Set<BlastProDomLocation> locations) {
        super(model, locations);
    }

}
